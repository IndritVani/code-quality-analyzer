package com.analyzer.analyzer.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CouplingExtractorTest {

    @TempDir
    Path dir;

    @Test
    void countsDistinctExternalTypesExcludingSelfAndJavaLang() throws IOException {
        String code = String.join("\n",
                "import java.util.List;",
                "import java.util.Map;",
                "public class Sample {",
                "  List<String> items;",            // List (String excluded as java.lang)
                "  Map<String, Foo> lookup;",        // Map, Foo
                "  Sample helper;",                  // self-reference, excluded
                "  Bar doThing() { return new Bar(); }", // Bar
                "}");

        var parsed = ExtractorTestSupport.parse(dir, code);
        FileMetricResult result = new CouplingExtractor().extract(parsed.cu(), parsed.file());

        // distinct external: List, Map, Foo, Bar
        assertThat(result.coupling()).isEqualTo(4);
    }
}
