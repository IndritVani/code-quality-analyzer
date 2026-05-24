package com.analyzer.service;

import com.analyzer.analyzer.AnalyzerEngine;
import com.analyzer.analyzer.pipeline.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Runs analysis off the request thread. Each DB transition delegates to {@link AnalysisService}
 * (a separate bean) so transactional boundaries apply correctly under {@code @Async}.
 *
 * <p>For GitHub-sourced projects the repo is shallow-cloned to a temp directory before analysis
 * and deleted afterward; local projects are analyzed in place.
 */
@Component
public class AnalysisRunner {

    private static final Logger log = LoggerFactory.getLogger(AnalysisRunner.class);

    private final AnalysisService analysisService;
    private final AnalyzerEngine analyzerEngine;
    private final GitRepositoryService gitRepositoryService;

    public AnalysisRunner(AnalysisService analysisService,
                          AnalyzerEngine analyzerEngine,
                          GitRepositoryService gitRepositoryService) {
        this.analysisService = analysisService;
        this.analyzerEngine = analyzerEngine;
        this.gitRepositoryService = gitRepositoryService;
    }

    @Async
    public void run(UUID runId) {
        Path cloneDir = null;
        try {
            RunTarget target = analysisService.beginRun(runId);
            Path projectRoot;
            if (target.isGithub()) {
                cloneDir = gitRepositoryService.cloneToTempDir(target.repoUrl());
                projectRoot = cloneDir;
            } else {
                projectRoot = Path.of(target.path());
            }
            AnalysisResult result = analyzerEngine.run(projectRoot);
            analysisService.completeRun(runId, result);
        } catch (Exception e) {
            log.error("Analysis run {} failed", runId, e);
            analysisService.failRun(runId, e.getMessage());
        } finally {
            if (cloneDir != null) {
                gitRepositoryService.cleanup(cloneDir);
            }
        }
    }
}
