package com.analyzer.dto;

import com.analyzer.domain.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequest(
        @NotBlank String name,
        String description,
        @NotBlank String path,
        @NotNull Tier tier,
        String language) {
}
