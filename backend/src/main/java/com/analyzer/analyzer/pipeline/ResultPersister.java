package com.analyzer.analyzer.pipeline;

import com.analyzer.analyzer.metrics.FileMetricResult;
import com.analyzer.domain.AnalysisRun;
import com.analyzer.domain.FileMetrics;
import com.analyzer.domain.ProjectMetrics;
import com.analyzer.repository.FileMetricsRepository;
import com.analyzer.repository.ProjectMetricsRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Writes one {@code project_metrics} row and N {@code file_metrics} rows for a completed run. */
@Component
public class ResultPersister {

    private final FileMetricsRepository fileMetricsRepository;
    private final ProjectMetricsRepository projectMetricsRepository;

    public ResultPersister(FileMetricsRepository fileMetricsRepository,
                           ProjectMetricsRepository projectMetricsRepository) {
        this.fileMetricsRepository = fileMetricsRepository;
        this.projectMetricsRepository = projectMetricsRepository;
    }

    public void persist(AnalysisRun run, AnalysisResult result) {
        List<FileMetrics> rows = new ArrayList<>(result.files().size());
        for (FileMetricResult file : result.files()) {
            FileMetrics row = new FileMetrics(run, file.getFilePath());
            row.setLoc(file.loc());
            row.setAvgCyclomatic(file.avgCyclomatic());
            row.setMaxCyclomatic(file.maxCyclomatic());
            row.setCoupling(file.coupling());
            row.setCommentDensity(file.commentDensity());
            rows.add(row);
        }
        fileMetricsRepository.saveAll(rows);

        ProjectMetricSummary summary = result.summary();
        ProjectMetrics metrics = new ProjectMetrics(run);
        metrics.setTotalLoc(summary.totalLoc());
        metrics.setAvgCyclomatic(summary.avgCyclomatic());
        metrics.setMaxCyclomatic(summary.maxCyclomatic());
        metrics.setDuplicationRatio(summary.duplicationRatio());
        metrics.setOutdatedDependencies(summary.outdatedDependencies());
        metrics.setAvgCoupling(summary.avgCoupling());
        metrics.setCommentDensity(summary.commentDensity());
        metrics.setMaintainabilityIndex(summary.maintainabilityIndex());
        projectMetricsRepository.save(metrics);
    }
}
