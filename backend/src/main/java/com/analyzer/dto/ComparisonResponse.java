package com.analyzer.dto;

import java.util.List;
import java.util.UUID;

public record ComparisonResponse(List<Entry> projects) {

    public record Entry(
            UUID projectId,
            String name,
            String tier,
            UUID latestRunId,
            ProjectMetricsResponse metrics) {
    }
}
