package com.analyzer.service;

import com.analyzer.domain.SourceType;

/**
 * What an analysis run should read: either a local directory ({@code path}) or a GitHub repo
 * to clone ({@code repoUrl}), depending on {@code sourceType}.
 */
public record RunTarget(SourceType sourceType, String path, String repoUrl) {

    public boolean isGithub() {
        return sourceType == SourceType.GITHUB;
    }
}
