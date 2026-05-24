package com.analyzer.service;

import com.analyzer.domain.AnalysisRun;
import com.analyzer.domain.Project;
import com.analyzer.domain.SourceType;
import com.analyzer.dto.CreateProjectRequest;
import com.analyzer.repository.AnalysisRunRepository;
import com.analyzer.repository.FileMetricsRepository;
import com.analyzer.repository.ProjectMetricsRepository;
import com.analyzer.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final ProjectMetricsRepository projectMetricsRepository;
    private final FileMetricsRepository fileMetricsRepository;
    private final GitRepositoryService gitRepositoryService;

    public ProjectService(ProjectRepository projectRepository,
                          AnalysisRunRepository analysisRunRepository,
                          ProjectMetricsRepository projectMetricsRepository,
                          FileMetricsRepository fileMetricsRepository,
                          GitRepositoryService gitRepositoryService) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.projectMetricsRepository = projectMetricsRepository;
        this.fileMetricsRepository = fileMetricsRepository;
        this.gitRepositoryService = gitRepositoryService;
    }

    @Transactional
    public Project create(CreateProjectRequest request) {
        SourceType sourceType = request.sourceType() == null ? SourceType.LOCAL : request.sourceType();

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setSourceType(sourceType);
        project.setTier(request.tier());
        if (request.language() != null && !request.language().isBlank()) {
            project.setLanguage(request.language());
        }

        if (sourceType == SourceType.GITHUB) {
            if (request.repoUrl() == null || request.repoUrl().isBlank()) {
                throw new BadRequestException("A GitHub repository URL is required for GitHub projects.");
            }
            // Validates and normalizes; throws BadRequestException on a malformed URL.
            project.setRepoUrl(gitRepositoryService.normalizeGithubUrl(request.repoUrl()));
        } else {
            if (request.path() == null || request.path().isBlank()) {
                throw new BadRequestException("A local repository path is required for local projects.");
            }
            project.setPath(request.path());
        }

        return projectRepository.save(project);
    }

    public List<Project> list() {
        return projectRepository.findAll();
    }

    public Project get(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    @Transactional
    public void delete(UUID id) {
        Project project = get(id);
        List<AnalysisRun> runs = analysisRunRepository.findByProjectIdOrderByStartedAtDesc(id);
        for (AnalysisRun run : runs) {
            projectMetricsRepository.deleteByAnalysisRunId(run.getId());
            fileMetricsRepository.deleteByAnalysisRunId(run.getId());
        }
        analysisRunRepository.deleteAll(runs);
        projectRepository.delete(project);
    }
}
