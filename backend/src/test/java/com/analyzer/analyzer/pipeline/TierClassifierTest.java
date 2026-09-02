package com.analyzer.analyzer.pipeline;

import com.analyzer.domain.Tier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TierClassifierTest {

    private final TierClassifier classifier = new TierClassifier();

    @Test
    void classifiesByMaintainabilityIndex() {
        assertThat(classifier.classify(85)).isEqualTo(Tier.WELL_MAINTAINED);
        assertThat(classifier.classify(60)).isEqualTo(Tier.AVERAGE);
        assertThat(classifier.classify(40)).isEqualTo(Tier.NEGLECTED);
    }

    @Test
    void boundariesAreInclusiveOnTheUpperTier() {
        assertThat(classifier.classify(75)).isEqualTo(Tier.WELL_MAINTAINED);
        assertThat(classifier.classify(50)).isEqualTo(Tier.AVERAGE);
        assertThat(classifier.classify(49.9)).isEqualTo(Tier.NEGLECTED);
        assertThat(classifier.classify(0)).isEqualTo(Tier.NEGLECTED);
        assertThat(classifier.classify(100)).isEqualTo(Tier.WELL_MAINTAINED);
    }
}
