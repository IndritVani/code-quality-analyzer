package com.analyzer.analyzer.pipeline;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ResultAggregatorTest {

    private final ResultAggregator aggregator = new ResultAggregator();

    @Test
    void classicBaseDominatesWhenNoPenalties() {
        // base = (171 - 5.2*ln(100) - 0.23*2 - 16.2*ln(30)) * 100/171 ≈ 53.505
        double mi = aggregator.maintainabilityIndex(100, 2, 30, 0.0, 0.0, 0.0);
        assertThat(mi).isCloseTo(53.505, within(0.01));
    }

    @Test
    void duplicationAndCouplingSubtractFromTheBase() {
        // base ≈ 53.505, full duplication (-15) and full coupling (-10) -> ≈ 28.505
        double mi = aggregator.maintainabilityIndex(100, 2, 30, 1.0, 20.0, 0.0);
        assertThat(mi).isCloseTo(28.505, within(0.01));
    }

    @Test
    void documentationAddsToTheBase() {
        // base ≈ 53.505 + full documentation bonus (+10) -> ≈ 63.505
        double mi = aggregator.maintainabilityIndex(100, 2, 30, 0.0, 0.0, 0.30);
        assertThat(mi).isCloseTo(63.505, within(0.01));
    }

    @Test
    void trivialFileScoresNearOneHundredWithoutInfinity() {
        // Volume and LOC near 0: ln is floored at 1, so no -Infinity; base clamps to 100.
        double mi = aggregator.maintainabilityIndex(0, 0, 0, 0.0, 0.0, 0.0);
        assertThat(mi).isEqualTo(100.0);
    }

    @Test
    void emptyProjectHasZeroIndex() {
        var summary = aggregator.aggregate(List.of(), 0.0, 0);
        assertThat(summary.maintainabilityIndex()).isEqualTo(0.0);
        assertThat(summary.avgVolume()).isEqualTo(0.0);
        assertThat(summary.avgLoc()).isEqualTo(0.0);
    }
}
