package com.analyzer.service;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches public GitHub repositories for analysis. Each clone is shallow ({@code depth=1}) and
 * lands in a fresh temp directory that the caller deletes via {@link #cleanup(Path)} once the
 * run finishes — nothing is kept on disk between analyses.
 */
@Service
public class GitRepositoryService {

    private static final Logger log = LoggerFactory.getLogger(GitRepositoryService.class);

    // Matches an optional github.com host prefix followed by owner/repo. The trailing ".git"
    // and trailing slashes are stripped before matching. GitHub names allow letters, digits,
    // '-', '_' and '.'.
    private static final Pattern OWNER_REPO = Pattern.compile(
            "(?i)^(?:https?://(?:www\\.)?github\\.com/|github\\.com/)?"
                    + "([A-Za-z0-9._-]+)/([A-Za-z0-9._-]+)$");

    /**
     * Validate and normalize a public GitHub reference to an HTTPS clone URL. Accepts
     * {@code https://github.com/owner/repo[.git]}, {@code github.com/owner/repo}, and the
     * {@code owner/repo} shorthand. Rejects anything else (e.g. SSH {@code git@}) with a
     * {@link BadRequestException}, since we only do unauthenticated HTTPS clones.
     */
    public String normalizeGithubUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("GitHub URL is required.");
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("git@") || trimmed.startsWith("ssh://")) {
            throw new BadRequestException(
                    "SSH URLs are not supported; use a public HTTPS GitHub URL "
                            + "like https://github.com/owner/repo");
        }
        // Normalize to a clean owner/repo: drop trailing slashes, then a trailing ".git".
        String candidate = trimmed.replaceAll("/+$", "");
        if (candidate.regionMatches(true, candidate.length() - 4, ".git", 0, 4)) {
            candidate = candidate.substring(0, candidate.length() - 4);
        }
        Matcher m = OWNER_REPO.matcher(candidate);
        if (!m.matches()) {
            throw new BadRequestException(
                    "Not a valid public GitHub repository URL: " + raw
                            + " (expected e.g. https://github.com/owner/repo)");
        }
        return "https://github.com/" + m.group(1) + "/" + m.group(2) + ".git";
    }

    /**
     * Shallow-clone the given repo into a fresh temp directory and return it. The caller must
     * {@link #cleanup(Path)} the directory once analysis finishes.
     */
    public Path cloneToTempDir(String repoUrl) {
        String url = normalizeGithubUrl(repoUrl);
        Path dir;
        try {
            dir = Files.createTempDirectory("analyzer-clone-");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create temp directory for clone", e);
        }
        log.info("Cloning {} into {}", url, dir);
        try (Git ignored = Git.cloneRepository()
                .setURI(url)
                .setDirectory(dir.toFile())
                .setDepth(1)
                .call()) {
            return dir;
        } catch (Exception e) {
            cleanup(dir); // don't leak the temp dir if the clone fails partway through
            throw new RuntimeException(friendlyCloneError(url, e), e);
        }
    }

    /**
     * GitHub answers both "repo doesn't exist" and "repo is private" with an auth challenge,
     * which JGit surfaces as a cryptic credentials message. Translate that into something a
     * user can act on; pass anything else through unchanged.
     */
    private static String friendlyCloneError(String url, Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("Authentication is required")
                || msg.contains("not authorized")
                || msg.contains("401")
                || msg.contains("not found")) {
            return "Could not clone " + url
                    + " — the repository was not found or is not public "
                    + "(only public GitHub repositories are supported).";
        }
        return "Failed to clone " + url + ": " + msg;
    }

    /**
     * Recursively delete a previously cloned temp directory. Never throws — cleanup failures are
     * logged but must not mask the analysis result. Clears the read-only attribute first so the
     * read-only pack files JGit writes can be removed on Windows.
     */
    public void cleanup(Path dir) {
        if (dir == null) {
            return;
        }
        try {
            if (!Files.exists(dir)) {
                return;
            }
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    deleteForcibly(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                    deleteForcibly(d);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Failed to clean up clone directory {}", dir, e);
        }
    }

    private static void deleteForcibly(Path p) throws IOException {
        File f = p.toFile();
        if (!f.canWrite()) {
            f.setWritable(true, false);
        }
        Files.deleteIfExists(p);
    }
}
