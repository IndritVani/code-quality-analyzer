package com.analyzer.dto;

import com.analyzer.domain.FileMetrics;

public record FileMetricsResponse(
        String filePath,
        int loc,
        double avgCyclomatic,
        double maxCyclomatic,
        int coupling,
        double commentDensity) {

    public static FileMetricsResponse from(FileMetrics m) {
        return new FileMetricsResponse(
                m.getFilePath(),
                m.getLoc(),
                m.getAvgCyclomatic(),
                m.getMaxCyclomatic(),
                m.getCoupling(),
                m.getCommentDensity());
    }
}
