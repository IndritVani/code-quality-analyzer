package com.analyzer.analyzer.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Project-level dependency health. Parses every {@code pom.xml} in the tree and counts declared
 * dependencies. When enabled, it makes a best-effort, timeout-bounded query to Maven Central to
 * flag dependencies pinned below the latest published version. Network failures never fail the
 * run — they simply leave a dependency uncounted as outdated. This is the deliberate extension
 * point for a real CVE / advisory feed.
 */
@Component
public class DependencyHealthAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(DependencyHealthAnalyzer.class);
    private static final Set<String> SKIP_DIRS = Set.of("target", "build", ".git", "node_modules");
    private static final int MAX_CHECKS = 50;

    private final ObjectMapper objectMapper;
    private final boolean checkEnabled;
    private final long timeoutMs;
    private final HttpClient httpClient;

    public DependencyHealthAnalyzer(
            ObjectMapper objectMapper,
            @Value("${analyzer.dependency-check.enabled:true}") boolean checkEnabled,
            @Value("${analyzer.dependency-check.timeout-ms:2000}") long timeoutMs) {
        this.objectMapper = objectMapper;
        this.checkEnabled = checkEnabled;
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public record DependencyHealthResult(int totalDependencies, int outdatedDependencies) {
    }

    public DependencyHealthResult analyze(Path projectRoot) {
        // Dedupe by group:artifact -> declared version (last one wins).
        Map<String, String> dependencies = new LinkedHashMap<>();
        for (Path pom : findPomFiles(projectRoot)) {
            collectDependencies(pom, dependencies);
        }

        int total = dependencies.size();
        if (!checkEnabled) {
            return new DependencyHealthResult(total, 0);
        }

        int outdated = 0;
        int checks = 0;
        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            if (checks >= MAX_CHECKS) {
                break;
            }
            String version = entry.getValue();
            if (version == null || version.isBlank() || version.startsWith("${")) {
                continue; // version managed by parent/BOM or a property — can't assess
            }
            checks++;
            String[] ga = entry.getKey().split(":", 2);
            String latest = latestVersion(ga[0], ga[1]);
            if (latest != null && !latest.isBlank() && !latest.equals(version)) {
                outdated++;
            }
        }
        return new DependencyHealthResult(total, outdated);
    }

    private java.util.List<Path> findPomFiles(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals("pom.xml"))
                    .filter(p -> isNotInSkippedDir(root.relativize(p)))
                    .toList();
        } catch (Exception e) {
            log.warn("Could not scan for pom.xml under {}: {}", root, e.getMessage());
            return java.util.List.of();
        }
    }

    private boolean isNotInSkippedDir(Path relative) {
        for (Path segment : relative) {
            if (SKIP_DIRS.contains(segment.toString())) {
                return false;
            }
        }
        return true;
    }

    private void collectDependencies(Path pom, Map<String, String> sink) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document doc = factory.newDocumentBuilder().parse(pom.toFile());

            NodeList deps = doc.getElementsByTagName("dependency");
            for (int i = 0; i < deps.getLength(); i++) {
                Node node = deps.item(i);
                if (!(node instanceof Element dep)) {
                    continue;
                }
                String group = childText(dep, "groupId");
                String artifact = childText(dep, "artifactId");
                String version = childText(dep, "version");
                if (group != null && artifact != null) {
                    sink.put(group + ":" + artifact, version);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse {}: {}", pom, e.getMessage());
        }
    }

    private String childText(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return null;
        }
        String text = nodes.item(0).getTextContent();
        return text == null ? null : text.trim();
    }

    private String latestVersion(String group, String artifact) {
        try {
            String query = "g:\"" + group + "\" AND a:\"" + artifact + "\"";
            String url = "https://search.maven.org/solrsearch/select?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&rows=1&wt=json";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }
            JsonNode docs = objectMapper.readTree(response.body()).path("response").path("docs");
            if (docs.isArray() && !docs.isEmpty()) {
                return docs.get(0).path("latestVersion").asText(null);
            }
        } catch (Exception e) {
            log.debug("Maven Central lookup failed for {}:{} — {}", group, artifact, e.getMessage());
        }
        return null;
    }
}
