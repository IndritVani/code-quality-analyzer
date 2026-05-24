package com.analyzer.repository;

import com.analyzer.domain.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, UUID> {

    List<AnalysisRun> findByProjectIdOrderByStartedAtDesc(UUID projectId);
}
