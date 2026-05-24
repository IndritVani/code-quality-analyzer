package com.analyzer.repository;

import com.analyzer.domain.ProjectMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectMetricsRepository extends JpaRepository<ProjectMetrics, UUID> {

    Optional<ProjectMetrics> findByAnalysisRunId(UUID analysisRunId);

    void deleteByAnalysisRunId(UUID analysisRunId);
}
