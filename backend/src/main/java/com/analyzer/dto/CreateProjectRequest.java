package com.analyzer.dto;

import com.analyzer.domain.SourceType;
import jakarta.validation.constraints.NotBlank;

/**
 * Project creation request. Exactly one source is used depending on {@code sourceType}
 * (defaults to LOCAL when null): {@code path} for LOCAL, {@code repoUrl} for GITHUB.
 * That conditional requirement is enforced in {@code ProjectService.create}.
 *
 * <p>Tier is intentionally not accepted here — it is derived from the analysis results
 * (see {@code TierClassifier}); a new project starts {@code UNRATED} until first analyzed.
 */
public record CreateProjectRequest(
        @NotBlank String name,
        String description,
        String path,
        String repoUrl,
        SourceType sourceType,
        String language) {
}
