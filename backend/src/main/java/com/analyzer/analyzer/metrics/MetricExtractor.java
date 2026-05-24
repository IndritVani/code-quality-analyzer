package com.analyzer.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;

import java.nio.file.Path;

/**
 * A per-file metric. Each implementation runs independently over a parsed AST and returns a
 * {@link FileMetricResult} with only the fields it is responsible for populated.
 */
public interface MetricExtractor {

    String getMetricName();

    FileMetricResult extract(CompilationUnit ast, Path filePath);
}
