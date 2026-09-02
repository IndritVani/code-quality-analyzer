package com.analyzer.analyzer.pipeline;

import com.analyzer.config.MaintainabilityProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ResultAggregatorTest {

    private final ResultAggregator aggregator = new ResultAggregator(MaintainabilityProperties.defaults());

    // Mid-range inputs: raw SEI base ≈ 32.59, which the default remap (4 + 1.8·base) lifts to
    // displayBase ≈ 62.65 — comfortably away from the 0/100 clamps so the formula shape is testable.
    private static final double MID_VOLUME = 2000;
    private static final double MID_CC = 5;
    private static final double MID_LOC = 100;
    private static final double MID_DISPLAY_BASE = 62.654;

    @Test
    void remapsRawSeiBaseOntoDisplayRange() {
        double mi = aggregator.maintainabilityIndex(MID_VOLUME, MID_CC, MID_LOC, 0.0, 0.0, 0.0);
        assertThat(mi).isCloseTo(MID_DISPLAY_BASE, within(0.1));
    }

    @Test
    void duplicationAndCouplingSubtractFromTheDisplayBase() {
        // displayBase ≈ 62.65, full duplication (-15) and full coupling (-10) -> ≈ 37.65
        double mi = aggregator.maintainabilityIndex(MID_VOLUME, MID_CC, MID_LOC, 1.0, 20.0, 0.0);
        assertThat(mi).isCloseTo(MID_DISPLAY_BASE - 25, within(0.1));
    }

    @Test
    void documentationAddsToTheDisplayBase() {
        // displayBase ≈ 62.65 + full documentation bonus (+10) -> ≈ 72.65
        double mi = aggregator.maintainabilityIndex(MID_VOLUME, MID_CC, MID_LOC, 0.0, 0.0, 0.30);
        assertThat(mi).isCloseTo(MID_DISPLAY_BASE + 10, within(0.1));
    }

    @Test
    void cleanCodeClampsToOneHundredAfterRemap() {
        // Clean code (raw SEI base ≈ 53.5) remaps to 4 + 1.8·53.5 ≈ 100.3, clamped to 100.
        double mi = aggregator.maintainabilityIndex(100, 2, 30, 0.0, 0.0, 0.0);
        assertThat(mi).isEqualTo(100.0);
    }

    @Test
    void trivialFileScoresOneHundredWithoutInfinity() {
        // Volume and LOC near 0: ln is floored at 1, so no -Infinity; base + remap clamp to 100.
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

    @Test
    void configuredWeightsActuallyMoveTheScore() {
        // Double the duplication weight (15 -> 30): the same inputs drop by an extra 15 points.
        MaintainabilityProperties d = MaintainabilityProperties.defaults();
        MaintainabilityProperties custom = new MaintainabilityProperties(
                1.0, d.base(), d.scale(), 30.0, 10.0, 10.0, 20.0, 0.30);
        ResultAggregator harsh = new ResultAggregator(custom);

        // displayBase ≈ 62.65 - dup(30) - coupling(10) -> ≈ 22.65
        double mi = harsh.maintainabilityIndex(MID_VOLUME, MID_CC, MID_LOC, 1.0, 20.0, 0.0);
        assertThat(mi).isCloseTo(MID_DISPLAY_BASE - 40, within(0.1));
    }

    @Test
    void configuredScaleSlopeMovesTheScore() {
        // A gentler slope (1.8 -> 1.0) lowers the display for the same raw SEI base (≈ 32.59):
        // displayBase = 4 + 1.0·32.59 ≈ 36.59.
        MaintainabilityProperties d = MaintainabilityProperties.defaults();
        MaintainabilityProperties gentle = new MaintainabilityProperties(
                1.0, d.base(), new MaintainabilityProperties.Scale(4.0, 1.0),
                15.0, 10.0, 10.0, 20.0, 0.30);
        ResultAggregator flat = new ResultAggregator(gentle);

        double mi = flat.maintainabilityIndex(MID_VOLUME, MID_CC, MID_LOC, 0.0, 0.0, 0.0);
        assertThat(mi).isCloseTo(36.586, within(0.1));
    }
}
