package com.analyzer.api;

import com.analyzer.config.MaintainabilityProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only view of the analyzer's runtime configuration. Used by the frontend Score Breakdown
 * to render contribution bars against the currently active weights instead of re-hardcoding them
 * in JS.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final MaintainabilityProperties maintainabilityProperties;

    public ConfigController(MaintainabilityProperties maintainabilityProperties) {
        this.maintainabilityProperties = maintainabilityProperties;
    }

    @GetMapping("/maintainability")
    public MaintainabilityProperties maintainability() {
        return maintainabilityProperties;
    }
}
