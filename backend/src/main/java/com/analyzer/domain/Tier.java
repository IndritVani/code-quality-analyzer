package com.analyzer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Tier {
    WELL_MAINTAINED,
    AVERAGE,
    NEGLECTED,
    /** Assigned to projects that have not yet been analyzed; the analysis derives the rest. */
    UNRATED;

    /** Accept tier values case-insensitively from the API (e.g. "well_maintained"). */
    @JsonCreator
    public static Tier from(String value) {
        return Tier.valueOf(value.trim().toUpperCase());
    }
}
