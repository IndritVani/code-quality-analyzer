package com.analyzer.analyzer.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocExtractorTest {

    @TempDir
    Path dir;

    @Test
    void countsRawAndEffectiveLines() throws IOException {
        String code = String.join("\n",
                "package x;",                       // 1  code
                "// a comment",                     // 2  comment
                "public class Sample {",            // 3  code
                "",                                 // 4  blank
                "    int field = 1; // inline",     // 5  code + trailing comment
                "    void m() {",                   // 6  code
                "    }",                            // 7  code
                "}");                               // 8  code

        var parsed = ExtractorTestSupport.parse(dir, code);
        FileMetricResult result = new LocExtractor().extract(parsed.cu(), parsed.file());

        assertThat(result.loc()).isEqualTo(8);
        // Effective excludes the blank line (4), the comment line (2), and the line whose
        // content is shared with a trailing comment (5) — documented heuristic.
        assertThat(result.effectiveLoc()).isEqualTo(5);
    }
}
