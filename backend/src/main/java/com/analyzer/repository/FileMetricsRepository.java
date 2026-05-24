package com.analyzer.repository;

import com.analyzer.domain.FileMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileMetricsRepository extends JpaRepository<FileMetrics, UUID> {

    List<FileMetrics> findByAnalysisRunIdOrderByFilePath(UUID analysisRunId);

    void deleteByAnalysisRunId(UUID analysisRunId);
}
