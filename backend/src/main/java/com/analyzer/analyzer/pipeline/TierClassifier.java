package com.analyzer.analyzer.pipeline;

import org.springframework.stereotype.Component;

import com.analyzer.domain.Tier;

/**
 * Derives a project {@link Tier} from its Maintainability Index. The thresholds mirror the
 * score-color breakpoints in the frontend's {@code lib/metrics.js#scoreClass}, so a project's
 * tier badge always agrees with its maintainability-score color.
 *
 * <p>Calibrated for the re-scaled display MI (see {@link ResultAggregator} / the
 * {@code analyzer.mi.scale} remap): good code lands ~85, so WELL_MAINTAINED starts at 75 and
 * AVERAGE at 50.
 */
@Component
public class TierClassifier {

    static final double WELL_MAINTAINED_MIN = 75.0;
    static final double AVERAGE_MIN = 50.0;

    public Tier classify(double maintainabilityIndex) {
        if (maintainabilityIndex >= WELL_MAINTAINED_MIN) {
            return Tier.WELL_MAINTAINED;
        }
        if (maintainabilityIndex >= AVERAGE_MIN) {
            return Tier.AVERAGE;
        }
        return Tier.NEGLECTED;
    }
}
