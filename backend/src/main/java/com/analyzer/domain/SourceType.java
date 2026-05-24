package com.analyzer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Where a project's code comes from: an existing local directory or a GitHub repo to clone. */
public enum SourceType {
    LOCAL,
    GITHUB;

    /** Accept source-type values case-insensitively from the API (e.g. "github"). */
    @JsonCreator
    public static SourceType from(String value) {
        return SourceType.valueOf(value.trim().toUpperCase());
    }
}
