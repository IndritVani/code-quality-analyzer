package com.analyzer.dto;

import com.analyzer.domain.AnalysisRun;
import com.analyzer.domain.ProjectMetrics;

import java.time.Instant;
import java.util.UUID;

public record RunResponse(
        UUID id,
        UUID projectId,
        String status,
        Instant startedAt,
        Instant completedAt,
        String errorMessage,
        ProjectMetricsResponse metrics) {

    public static RunResponse from(AnalysisRun run, ProjectMetrics metrics) {
        return new RunResponse(
                run.getId(),
                run.getProject().getId(),
                run.getStatus().name().toLowerCase(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getErrorMessage(),
                metrics == null ? null : ProjectMetricsResponse.from(metrics));
    }
}
