package com.analyzer.api;

import com.analyzer.domain.AnalysisRun;
import com.analyzer.dto.FileMetricsResponse;
import com.analyzer.dto.RunResponse;
import com.analyzer.service.AnalysisRunner;
import com.analyzer.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisRunner analysisRunner;

    public AnalysisController(AnalysisService analysisService, AnalysisRunner analysisRunner) {
        this.analysisService = analysisService;
        this.analysisRunner = analysisRunner;
    }

    /** Kick off an analysis run; returns 202 with the PENDING run to poll. */
    @PostMapping("/projects/{id}/analyze")
    public ResponseEntity<RunResponse> analyze(@PathVariable UUID id) {
        AnalysisRun run = analysisService.startAnalysis(id);
        analysisRunner.run(run.getId());
        return ResponseEntity.accepted().body(RunResponse.from(run, null));
    }

    @GetMapping("/projects/{id}/runs")
    public List<RunResponse> runs(@PathVariable UUID id) {
        return analysisService.runsForProject(id);
    }

    @GetMapping("/runs/{runId}")
    public RunResponse run(@PathVariable UUID runId) {
        return analysisService.getRun(runId);
    }

    @GetMapping("/runs/{runId}/files")
    public List<FileMetricsResponse> files(@PathVariable UUID runId) {
        return analysisService.fileMetricsForRun(runId);
    }
}
