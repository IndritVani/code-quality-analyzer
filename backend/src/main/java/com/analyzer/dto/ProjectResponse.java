package com.analyzer.dto;

import com.analyzer.domain.Project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String path,
        String tier,
        String language,
        Instant analyzedAt,
        Instant createdAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getPath(),
                project.getTier().name().toLowerCase(),
                project.getLanguage(),
                project.getAnalyzedAt(),
                project.getCreatedAt());
    }
}
