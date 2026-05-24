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
 */
@Component
public class AnalysisRunner {

    private static final Logger log = LoggerFactory.getLogger(AnalysisRunner.class);

    private final AnalysisService analysisService;
    private final AnalyzerEngine analyzerEngine;

    public AnalysisRunner(AnalysisService analysisService, AnalyzerEngine analyzerEngine) {
        this.analysisService = analysisService;
        this.analyzerEngine = analyzerEngine;
    }

    @Async
    public void run(UUID runId) {
        try {
            String projectPath = analysisService.beginRun(runId);
            AnalysisResult result = analyzerEngine.run(Path.of(projectPath));
            analysisService.completeRun(runId, result);
        } catch (Exception e) {
            log.error("Analysis run {} failed", runId, e);
            analysisService.failRun(runId, e.getMessage());
        }
    }
}
