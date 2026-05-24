package com.analyzer.analyzer.pipeline;

import com.analyzer.analyzer.metrics.FileMetricResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rolls per-file results into a project summary and computes the Maintainability Index.
 *
 * <p>The index is a transparent 0–100 composite (higher is better) of the metrics we already
 * compute, with documented weights and saturation caps so the score is bounded and reproducible:
 * complexity, duplication, and coupling subtract; documentation adds.
 */
@Component
public class ResultAggregator {

    // Saturation caps: values at/above these contribute the full penalty.
    private static final double CYCLOMATIC_CAP = 25.0;
    private static final double COUPLING_CAP = 20.0;
    private static final double COMMENT_CAP = 0.30;

    public ProjectMetricSummary aggregate(List<FileMetricResult> files,
                                          double duplicationRatio,
                                          int outdatedDependencies) {
        if (files.isEmpty()) {
            return new ProjectMetricSummary(0, 0, 0, duplicationRatio, outdatedDependencies, 0, 0, 0);
        }

        int totalLoc = files.stream().mapToInt(FileMetricResult::loc).sum();
        double avgCyclomatic = files.stream().mapToDouble(FileMetricResult::avgCyclomatic).average().orElse(0);
        double maxCyclomatic = files.stream().mapToDouble(FileMetricResult::maxCyclomatic).max().orElse(0);
        double avgCoupling = files.stream().mapToInt(FileMetricResult::coupling).average().orElse(0);
        double commentDensity = files.stream().mapToDouble(FileMetricResult::commentDensity).average().orElse(0);

        double mi = maintainabilityIndex(avgCyclomatic, duplicationRatio, avgCoupling, commentDensity);

        return new ProjectMetricSummary(
                totalLoc, avgCyclomatic, maxCyclomatic, duplicationRatio,
                outdatedDependencies, avgCoupling, commentDensity, mi);
    }

    double maintainabilityIndex(double avgCyclomatic, double duplicationRatio,
                                double avgCoupling, double commentDensity) {
        double complexityPenalty = 0.40 * Math.min(avgCyclomatic, CYCLOMATIC_CAP) / CYCLOMATIC_CAP * 100;
        double duplicationPenalty = 0.25 * Math.min(duplicationRatio, 1.0) * 100;
        double couplingPenalty = 0.20 * Math.min(avgCoupling, COUPLING_CAP) / COUPLING_CAP * 100;
        double documentationBonus = 0.15 * Math.min(commentDensity, COMMENT_CAP) / COMMENT_CAP * 100;

        double score = 100 - complexityPenalty - duplicationPenalty - couplingPenalty + documentationBonus;
        return Math.max(0, Math.min(100, score));
    }
}
