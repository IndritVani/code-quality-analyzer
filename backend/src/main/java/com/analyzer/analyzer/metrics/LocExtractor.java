package com.analyzer.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Lines of code. Raw LOC is the physical line count; effective LOC excludes blank lines and
 * lines occupied by comments.
 */
@Component
public class LocExtractor implements MetricExtractor {

    @Override
    public String getMetricName() {
        return "loc";
    }

    @Override
    public FileMetricResult extract(CompilationUnit ast, Path filePath) {
        List<String> lines = AstSupport.readLines(filePath);
        Set<Integer> commentLines = AstSupport.commentLineNumbers(ast);

        int effective = 0;
        for (int i = 0; i < lines.size(); i++) {
            boolean blank = lines.get(i).isBlank();
            boolean comment = commentLines.contains(i + 1);
            if (!blank && !comment) {
                effective++;
            }
        }

        FileMetricResult result = new FileMetricResult(filePath.toString());
        result.setLoc(lines.size());
        result.setEffectiveLoc(effective);
        return result;
    }
}
