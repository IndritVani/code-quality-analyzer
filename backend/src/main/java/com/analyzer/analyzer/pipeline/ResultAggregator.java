package com.analyzer.analyzer.pipeline;

import com.analyzer.analyzer.metrics.FileMetricResult;
import com.analyzer.config.MaintainabilityProperties;
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
 *
 * <p>Because the raw SEI base compresses good code into ~40–55, it is linearly remapped onto an
 * intuitive 0–100 display range (see {@link MaintainabilityProperties.Scale}) before the secondary
 * adjustments are applied.
 *
 * <p>All coefficients, weights, caps, and the display scale come from {@link MaintainabilityProperties}
 * (bound to {@code analyzer.mi.*} in {@code application.yml}).
 */
@Component
public class ResultAggregator {

    private final MaintainabilityProperties weights;

    public ResultAggregator(MaintainabilityProperties weights) {
        this.weights = weights;
    }

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
        MaintainabilityProperties.Base b = weights.base();

        // Classic SEI base, normalized to 0–100 by the (configurable) intercept so "no penalties"
        // still tops out at 100 regardless of how the intercept is tuned. ln args floored at 1
        // so trivial files (V/LOC ≈ 0) can't produce -Infinity.
        double norm = (b.constant()
                - b.volumeCoefficient() * Math.log(Math.max(avgVolume, 1.0))
                - b.complexityCoefficient() * avgCyclomatic
                - b.locCoefficient() * Math.log(Math.max(avgLoc, 1.0))) * 100.0 / b.constant();
        double seiBase = Math.max(0, Math.min(100, weights.baseWeight() * norm));

        // The raw SEI base compresses good code into ~40–55; remap it linearly onto an intuitive
        // 0–100 display range before the secondary adjustments (which stay in display points).
        MaintainabilityProperties.Scale scale = weights.scale();
        double displayBase = Math.max(0, Math.min(100, scale.offset() + scale.slope() * seiBase));

        // Secondary adjustments for signals the classic formula does not see.
        double duplicationPenalty = weights.duplicationWeight() * Math.min(duplicationRatio, 1.0);
        double couplingPenalty = weights.couplingWeight()
                * Math.min(avgCoupling, weights.couplingCap()) / weights.couplingCap();
        double documentationBonus = weights.documentationWeight()
                * Math.min(commentDensity, weights.commentCap()) / weights.commentCap();

        double score = displayBase - duplicationPenalty - couplingPenalty + documentationBonus;
        return Math.max(0, Math.min(100, score));
    }
}
