package com.analyzer.api;

import com.analyzer.dto.ComparisonResponse;
import com.analyzer.service.AnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ComparisonController {

    private final AnalysisService analysisService;

    public ComparisonController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/compare")
    public ComparisonResponse compare(@RequestParam("ids") List<UUID> ids) {
        return analysisService.compare(ids);
    }
}
