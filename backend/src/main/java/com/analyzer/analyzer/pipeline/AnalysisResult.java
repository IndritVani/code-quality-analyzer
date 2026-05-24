package com.analyzer.analyzer.pipeline;

import com.analyzer.analyzer.metrics.FileMetricResult;

import java.util.List;

/** In-memory output of one analyzer run: per-file results plus the project rollup. */
public record AnalysisResult(List<FileMetricResult> files, ProjectMetricSummary summary) {
}
