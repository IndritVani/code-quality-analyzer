package com.analyzer.analyzer.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CommentDensityExtractorTest {

    @TempDir
    Path dir;

    @Test
    void computesCommentLineRatio() throws IOException {
        String code = String.join("\n",
                "package x;",                    // 1  code
                "// a comment",                  // 2  comment
                "public class Sample {",         // 3  code
                "",                              // 4  blank
                "    int field = 1; // inline",  // 5  comment (trailing)
                "    void m() {",                // 6  code
                "    }",                         // 7  code
                "}");                            // 8  code

        var parsed = ExtractorTestSupport.parse(dir, code);
        FileMetricResult result = new CommentDensityExtractor().extract(parsed.cu(), parsed.file());

        // comment lines {2, 5} over 8 physical lines
        assertThat(result.commentDensity()).isEqualTo(0.25);
    }
}
