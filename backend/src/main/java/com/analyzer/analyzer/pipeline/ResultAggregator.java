package com.analyzer.analyzer.pipeline;

import com.analyzer.analyzer.metrics.FileMetricResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rolls per-file results into a project summary and computes the Maintainability Index.
 *
 * <p>The index is a <b>layered</b> 0–100 score (higher is better). The base is the classic
 * Coleman–Oman / SEI Maintainability Index (3-metric form, normalized the way Visual Studio
 * does), which captures intrinsic maintainability from Halstead Volume, cyclomatic complexity,
 * and size (LOC). On top of that base we apply the signals the classic formula ignores —
 * duplication and coupling subtract, documentation adds — kept deliberately secondary so the
 * intrinsic score stays dominant.
 */
@Component
public class ResultAggregator {

    // Saturation caps: values at/above these contribute the full adjustment.
    private static final double COUPLING_CAP = 20.0;
    private static final double COMMENT_CAP = 0.30;

    // Maximum points each secondary signal can move the score by.
    private static final double DUPLICATION_WEIGHT = 15.0;
    private static final double COUPLING_WEIGHT = 10.0;
    private static final double DOCUMENTATION_WEIGHT = 10.0;

    public ProjectMetricSummary aggregate(List<FileMetricResult> files,
                                          double duplicationRatio,
                                          int outdatedDependencies) {
        if (files.isEmpty()) {
            return new ProjectMetricSummary(0, 0, 0, duplicationRatio, outdatedDependencies, 0, 0, 0, 0, 0);
        }

        int totalLoc = files.stream().mapToInt(FileMetricResult::loc).sum();
        double avgCyclomatic = files.stream().mapToDouble(FileMetricResult::avgCyclomatic).average().orElse(0);
        double maxCyclomatic = files.stream().mapToDouble(FileMetricResult::maxCyclomatic).max().orElse(0);
        double avgCoupling = files.stream().mapToInt(FileMetricResult::coupling).average().orElse(0);
        double commentDensity = files.stream().mapToDouble(FileMetricResult::commentDensity).average().orElse(0);
        double avgVolume = files.stream().mapToDouble(FileMetricResult::volume).average().orElse(0);
        double avgLoc = files.stream().mapToInt(FileMetricResult::effectiveLoc).average().orElse(0);

        double mi = maintainabilityIndex(avgVolume, avgCyclomatic, avgLoc,
                duplicationRatio, avgCoupling, commentDensity);

        return new ProjectMetricSummary(
                totalLoc, avgCyclomatic, maxCyclomatic, duplicationRatio,
                outdatedDependencies, avgCoupling, commentDensity, avgVolume, avgLoc, mi);
    }

    double maintainabilityIndex(double avgVolume, double avgCyclomatic, double avgLoc,
                                double duplicationRatio, double avgCoupling, double commentDensity) {
        // Classic SEI base, normalized to 0–100. ln arguments are floored at 1 so trivial files
        // (Volume/LOC near 0) can't produce -Infinity.
        double base = (171.0
                - 5.2 * Math.log(Math.max(avgVolume, 1.0))
                - 0.23 * avgCyclomatic
                - 16.2 * Math.log(Math.max(avgLoc, 1.0))) * 100.0 / 171.0;
        base = Math.max(0, Math.min(100, base));

        // Secondary adjustments for signals the classic formula does not see.
        double duplicationPenalty = DUPLICATION_WEIGHT * Math.min(duplicationRatio, 1.0);
        double couplingPenalty = COUPLING_WEIGHT * Math.min(avgCoupling, COUPLING_CAP) / COUPLING_CAP;
        double documentationBonus = DOCUMENTATION_WEIGHT * Math.min(commentDensity, COMMENT_CAP) / COMMENT_CAP;

        double score = base - duplicationPenalty - couplingPenalty + documentationBonus;
        return Math.max(0, Math.min(100, score));
    }
}
