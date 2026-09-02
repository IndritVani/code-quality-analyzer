package com.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Tunable weights for the Maintainability Index computed in
 * {@link com.analyzer.analyzer.pipeline.ResultAggregator}. Bound from {@code analyzer.mi.*} in
 * {@code application.yml}; defaults below reproduce the original hardcoded score, so behavior is
 * unchanged until something is overridden.
 *
 * <p>Edit the yaml and restart the backend to apply new weights; existing runs keep their
 * historical MI (it's persisted), but a fresh analysis will use the new values.
 */
@ConfigurationProperties("analyzer.mi")
public record MaintainabilityProperties(
        @DefaultValue("1.0") double baseWeight,
        @DefaultValue Base base,
        @DefaultValue Scale scale,
        @DefaultValue("15.0") double duplicationWeight,
        @DefaultValue("10.0") double couplingWeight,
        @DefaultValue("10.0") double documentationWeight,
        @DefaultValue("20.0") double couplingCap,
        @DefaultValue("0.30") double commentCap) {

    /** Coefficients of the classic Coleman–Oman / SEI base formula. */
    public record Base(
            @DefaultValue("171.0") double constant,
            @DefaultValue("5.2") double volumeCoefficient,
            @DefaultValue("0.23") double complexityCoefficient,
            @DefaultValue("16.2") double locCoefficient) {
    }

    /**
     * Linear remap applied to the raw SEI base ({@code display = offset + slope · seiBase}) so the
     * displayed score spreads across an intuitive 0–100 range. The classic SEI formula compresses
     * good code into ~40–55; the defaults (offset 4, slope 1.8) stretch that so clean code lands
     * ~85 and weak code ~40. Tune these to re-anchor the scale without touching the SEI coefficients.
     */
    public record Scale(
            @DefaultValue("4.0") double offset,
            @DefaultValue("1.8") double slope) {
    }

    /** Construct the canonical defaults. */
    public static MaintainabilityProperties defaults() {
        return new MaintainabilityProperties(
                1.0,
                new Base(171.0, 5.2, 0.23, 16.2),
                new Scale(4.0, 1.8),
                15.0, 10.0, 10.0, 20.0, 0.30);
    }
}
