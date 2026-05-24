package com.analyzer.analyzer.pipeline;

import com.analyzer.analyzer.SourceFiles;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Project-level code duplication, approximated CPD-style: significant (non-blank, normalized)
 * lines are scanned with a sliding window; any window of {@value #WINDOW} lines whose content
 * is seen more than once marks its lines as duplicated. The ratio is duplicated significant
 * lines over total significant lines (0.0–1.0).
 */
@Component
public class DuplicationAnalyzer {

    static final int WINDOW = 6;

    public double duplicationRatio(List<Path> files) {
        List<List<String>> perFile = new ArrayList<>();
        int totalSignificant = 0;
        for (Path file : files) {
            List<String> significant = new ArrayList<>();
            for (String line : SourceFiles.readLines(file)) {
                String normalized = line.strip();
                if (!normalized.isEmpty()) {
                    significant.add(normalized);
                }
            }
            perFile.add(significant);
            totalSignificant += significant.size();
        }

        if (totalSignificant == 0) {
            return 0.0;
        }

        // Count how often each window hash appears across the whole project.
        Map<String, Integer> windowCounts = new HashMap<>();
        for (List<String> significant : perFile) {
            for (int start = 0; start + WINDOW <= significant.size(); start++) {
                String key = String.join("\n", significant.subList(start, start + WINDOW));
                windowCounts.merge(key, 1, Integer::sum);
            }
        }

        // Mark every significant line that belongs to a window seen more than once.
        int duplicatedLines = 0;
        for (List<String> significant : perFile) {
            Set<Integer> duplicatedIndices = new HashSet<>();
            for (int start = 0; start + WINDOW <= significant.size(); start++) {
                String key = String.join("\n", significant.subList(start, start + WINDOW));
                if (windowCounts.getOrDefault(key, 0) > 1) {
                    for (int i = start; i < start + WINDOW; i++) {
                        duplicatedIndices.add(i);
                    }
                }
            }
            duplicatedLines += duplicatedIndices.size();
        }

        return (double) duplicatedLines / totalSignificant;
    }
}
