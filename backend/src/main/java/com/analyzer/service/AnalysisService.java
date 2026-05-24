package com.analyzer.service;

import com.analyzer.analyzer.pipeline.AnalysisResult;
import com.analyzer.analyzer.pipeline.ResultPersister;
import com.analyzer.domain.AnalysisRun;
import com.analyzer.domain.Project;
import com.analyzer.domain.ProjectMetrics;
import com.analyzer.domain.RunStatus;
import com.analyzer.dto.ComparisonResponse;
import com.analyzer.dto.FileMetricsResponse;
import com.analyzer.dto.RunResponse;
import com.analyzer.repository.AnalysisRunRepository;
import com.analyzer.repository.FileMetricsRepository;
import com.analyzer.repository.ProjectMetricsRepository;
import com.analyzer.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalysisService {

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final ProjectMetricsRepository projectMetricsRepository;
    private final FileMetricsRepository fileMetricsRepository;
    private final ResultPersister resultPersister;

    public AnalysisService(ProjectRepository projectRepository,
                           AnalysisRunRepository analysisRunRepository,
                           ProjectMetricsRepository projectMetricsRepository,
                           FileMetricsRepository fileMetricsRepository,
                           ResultPersister resultPersister) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.projectMetricsRepository = projectMetricsRepository;
        this.fileMetricsRepository = fileMetricsRepository;
        this.resultPersister = resultPersister;
    }

    /** Create a PENDING run and commit it so the async worker can pick it up. */
    @Transactional
    public AnalysisRun startAnalysis(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        return analysisRunRepository.save(new AnalysisRun(project));
    }

    /** Transition to RUNNING and return what the run should analyze (local path or GitHub repo). */
    @Transactional
    public RunTarget beginRun(UUID runId) {
        AnalysisRun run = requireRun(runId);
        run.setStatus(RunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        analysisRunRepository.save(run);
        Project project = run.getProject();
        return new RunTarget(project.getSourceType(), project.getPath(), project.getRepoUrl());
    }

    @Transactional
    public void completeRun(UUID runId, AnalysisResult result) {
        AnalysisRun run = requireRun(runId);
        resultPersister.persist(run, result);
        run.setStatus(RunStatus.COMPLETE);
        run.setCompletedAt(Instant.now());
        analysisRunRepository.save(run);

        Project project = run.getProject();
        project.setAnalyzedAt(Instant.now());
        projectRepository.save(project);
    }

    @Transactional
    public void failRun(UUID runId, String errorMessage) {
        AnalysisRun run = requireRun(runId);
        run.setStatus(RunStatus.FAILED);
        run.setErrorMessage(errorMessage);
        run.setCompletedAt(Instant.now());
        analysisRunRepository.save(run);
    }

    @Transactional(readOnly = true)
    public RunResponse getRun(UUID runId) {
        AnalysisRun run = requireRun(runId);
        ProjectMetrics metrics = projectMetricsRepository.findByAnalysisRunId(runId).orElse(null);
        return RunResponse.from(run, metrics);
    }

    @Transactional(readOnly = true)
    public List<RunResponse> runsForProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        List<RunResponse> responses = new ArrayList<>();
        for (AnalysisRun run : analysisRunRepository.findByProjectIdOrderByStartedAtDesc(projectId)) {
            ProjectMetrics metrics = projectMetricsRepository.findByAnalysisRunId(run.getId()).orElse(null);
            responses.add(RunResponse.from(run, metrics));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<FileMetricsResponse> fileMetricsForRun(UUID runId) {
        requireRun(runId);
        return fileMetricsRepository.findByAnalysisRunIdOrderByFilePath(runId).stream()
                .map(FileMetricsResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComparisonResponse compare(List<UUID> projectIds) {
        List<ComparisonResponse.Entry> entries = new ArrayList<>();
        for (UUID projectId : projectIds) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
            AnalysisRun latest = latestCompleteRun(projectId);
            ProjectMetrics metrics = latest == null
                    ? null
                    : projectMetricsRepository.findByAnalysisRunId(latest.getId()).orElse(null);
            entries.add(new ComparisonResponse.Entry(
                    project.getId(),
                    project.getName(),
                    project.getTier().name().toLowerCase(),
                    latest == null ? null : latest.getId(),
                    metrics == null ? null : com.analyzer.dto.ProjectMetricsResponse.from(metrics)));
        }
        return new ComparisonResponse(entries);
    }

    private AnalysisRun latestCompleteRun(UUID projectId) {
        return analysisRunRepository.findByProjectIdOrderByStartedAtDesc(projectId).stream()
                .filter(run -> run.getStatus() == RunStatus.COMPLETE)
                .findFirst()
                .orElse(null);
    }

    private AnalysisRun requireRun(UUID runId) {
        return analysisRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis run", runId));
    }
}
