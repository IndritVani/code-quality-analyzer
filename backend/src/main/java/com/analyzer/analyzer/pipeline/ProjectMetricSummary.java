package com.analyzer.analyzer.pipeline;

/** Project-level rollup produced by {@link ResultAggregator}. */
public record ProjectMetricSummary(
        int totalLoc,
        double avgCyclomatic,
        double maxCyclomatic,
        double duplicationRatio,
        int outdatedDependencies,
        double avgCoupling,
        double commentDensity,
        double maintainabilityIndex) {
}
