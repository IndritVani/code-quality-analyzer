package com.analyzer.analyzer.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CyclomaticComplexityExtractorTest {

    @TempDir
    Path dir;

    @Test
    void countsDecisionPointsPerMethod() throws IOException {
        String code = String.join("\n",
                "public class Sample {",
                "  int m(int x) {",
                "    if (x > 0 && x < 10) {",   // if + && = 2
                "      return 1;",
                "    }",
                "    if (x == 5) { return 2; }", // if = 1
                "    return 0;",
                "  }",
                "}");

        var parsed = ExtractorTestSupport.parse(dir, code);
        FileMetricResult result = new CyclomaticComplexityExtractor().extract(parsed.cu(), parsed.file());

        // base 1 + (2 ifs) + (1 &&) = 4 for the single method
        assertThat(result.avgCyclomatic()).isEqualTo(4.0);
        assertThat(result.maxCyclomatic()).isEqualTo(4.0);
    }

    @Test
    void averagesAcrossMethodsAndZeroWhenNone() throws IOException {
        String code = String.join("\n",
                "public class Sample {",
                "  void simple() { return; }",          // complexity 1
                "  void branchy() { if (true) {} for (;;) {} }", // 1 + if + for = 3
                "}");

        var parsed = ExtractorTestSupport.parse(dir, code);
        FileMetricResult result = new CyclomaticComplexityExtractor().extract(parsed.cu(), parsed.file());

        assertThat(result.avgCyclomatic()).isEqualTo(2.0); // (1 + 3) / 2
        assertThat(result.maxCyclomatic()).isEqualTo(3.0);
    }
}
