package com.analyzer.analyzer.pipeline;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Walks a project tree and collects the {@code .java} source files worth analyzing. */
@Component
public class FileDiscovery {

    private static final Set<String> SKIP_DIRS =
            Set.of("target", "build", ".git", "node_modules", ".idea", ".mvn", "out", "bin");

    public List<Path> discover(Path root) {
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Project path is not a directory: " + root);
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().endsWith("package-info.java"))
                    .filter(p -> isNotInSkippedDir(root, p))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to walk project tree: " + root, e);
        }
    }

    private boolean isNotInSkippedDir(Path root, Path file) {
        Path relative = root.relativize(file);
        for (Path segment : relative) {
            if (SKIP_DIRS.contains(segment.toString())) {
                return false;
            }
        }
        return true;
    }
}
