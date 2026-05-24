package com.analyzer.analyzer.metrics;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class ExtractorTestSupport {

    private ExtractorTestSupport() {
    }

    record Parsed(CompilationUnit cu, Path file) {
    }

    /** Write a Java snippet to a temp file and parse it, so extractors can read both AST and file. */
    static Parsed parse(Path dir, String code) throws IOException {
        Path file = dir.resolve("Sample.java");
        Files.writeString(file, code);
        return new Parsed(StaticJavaParser.parse(file), file);
    }
}
