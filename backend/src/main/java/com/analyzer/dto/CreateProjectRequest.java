package com.analyzer.dto;

import com.analyzer.domain.SourceType;
import com.analyzer.domain.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Project creation request. Exactly one source is used depending on {@code sourceType}
 * (defaults to LOCAL when null): {@code path} for LOCAL, {@code repoUrl} for GITHUB.
 * That conditional requirement is enforced in {@code ProjectService.create}.
 */
public record CreateProjectRequest(
        @NotBlank String name,
        String description,
        String path,
        String repoUrl,
        SourceType sourceType,
        @NotNull Tier tier,
        String language) {
}
