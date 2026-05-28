package com.analyzer.analyzer.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HalsteadVolumeExtractorTest {

    @TempDir
    Path dir;

    @Test
    void computesVolumeFromOperatorsAndOperands() throws IOException {
        String code = String.join("\n",
                "public class Sample {",
                "  int add(int a, int b) {",
                "    return a + b;",   // operators: "+" and "return"; operands: a, b
                "  }",
                "}");

        var parsed = ExtractorTestSupport.parse(dir, code);
        FileMetricResult result = new HalsteadVolumeExtractor().extract(parsed.cu(), parsed.file());

        // distinct operators {"+", "return"} = 2, distinct operands {"a", "b"} = 2  -> n = 4
        // total operators 2 + total operands 2 -> N = 4 ; Volume = 4 * log2(4) = 8.0
        assertThat(result.volume()).isCloseTo(8.0, within(1e-9));
    }

    @Test
    void emptyClassHasZeroVolume() throws IOException {
        var parsed = ExtractorTestSupport.parse(dir, "public class Empty {}");
        FileMetricResult result = new HalsteadVolumeExtractor().extract(parsed.cu(), parsed.file());

        assertThat(result.volume()).isEqualTo(0.0);
    }
}
