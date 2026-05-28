package com.analyzer.analyzer.pipeline;

import com.analyzer.domain.Tier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TierClassifierTest {

    private final TierClassifier classifier = new TierClassifier();

    @Test
    void classifiesByMaintainabilityIndex() {
        assertThat(classifier.classify(60)).isEqualTo(Tier.WELL_MAINTAINED);
        assertThat(classifier.classify(35)).isEqualTo(Tier.AVERAGE);
        assertThat(classifier.classify(15)).isEqualTo(Tier.NEGLECTED);
    }

    @Test
    void boundariesAreInclusiveOnTheUpperTier() {
        assertThat(classifier.classify(45)).isEqualTo(Tier.WELL_MAINTAINED);
        assertThat(classifier.classify(25)).isEqualTo(Tier.AVERAGE);
        assertThat(classifier.classify(24.9)).isEqualTo(Tier.NEGLECTED);
        assertThat(classifier.classify(0)).isEqualTo(Tier.NEGLECTED);
        assertThat(classifier.classify(100)).isEqualTo(Tier.WELL_MAINTAINED);
    }
}
