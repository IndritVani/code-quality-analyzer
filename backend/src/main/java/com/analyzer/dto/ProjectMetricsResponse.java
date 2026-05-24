package com.analyzer.dto;

import com.analyzer.domain.ProjectMetrics;

public record ProjectMetricsResponse(
        int totalLoc,
        double avgCyclomatic,
        double maxCyclomatic,
        double duplicationRatio,
        int outdatedDependencies,
        double avgCoupling,
        double commentDensity,
        double maintainabilityIndex) {

    public static ProjectMetricsResponse from(ProjectMetrics m) {
        return new ProjectMetricsResponse(
                m.getTotalLoc(),
                m.getAvgCyclomatic(),
                m.getMaxCyclomatic(),
                m.getDuplicationRatio(),
                m.getOutdatedDependencies(),
                m.getAvgCoupling(),
                m.getCommentDensity(),
                m.getMaintainabilityIndex());
    }
}
