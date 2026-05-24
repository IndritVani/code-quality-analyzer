package com.analyzer.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Documentation density: the fraction of physical lines occupied by Javadoc, block, or
 * line comments (0.0–1.0).
 */
@Component
public class CommentDensityExtractor implements MetricExtractor {

    @Override
    public String getMetricName() {
        return "comment_density";
    }

    @Override
    public FileMetricResult extract(CompilationUnit ast, Path filePath) {
        List<String> lines = AstSupport.readLines(filePath);
        Set<Integer> commentLines = AstSupport.commentLineNumbers(ast);

        int totalLines = Math.max(1, lines.size());
        double density = (double) commentLines.size() / totalLines;

        FileMetricResult result = new FileMetricResult(filePath.toString());
        result.setCommentDensity(density);
        return result;
    }
}
