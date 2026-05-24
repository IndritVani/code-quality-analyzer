package com.analyzer.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * McCabe cyclomatic complexity. Per method: 1 + the number of decision points
 * (if / for / while / do / case / catch / ternary / {@code &&} / {@code ||}).
 * Reports the average and max across all methods in the file.
 */
@Component
public class CyclomaticComplexityExtractor implements MetricExtractor {

    @Override
    public String getMetricName() {
        return "cyclomatic_complexity";
    }

    @Override
    public FileMetricResult extract(CompilationUnit ast, Path filePath) {
        List<CallableDeclaration> callables = ast.findAll(CallableDeclaration.class);

        double total = 0;
        double max = 0;
        for (CallableDeclaration<?> callable : callables) {
            int complexity = 1 + decisionPoints(callable);
            total += complexity;
            max = Math.max(max, complexity);
        }

        double avg = callables.isEmpty() ? 0.0 : total / callables.size();

        FileMetricResult result = new FileMetricResult(filePath.toString());
        result.setAvgCyclomatic(avg);
        result.setMaxCyclomatic(max);
        return result;
    }

    private int decisionPoints(CallableDeclaration<?> callable) {
        int count = 0;
        count += callable.findAll(IfStmt.class).size();
        count += callable.findAll(ForStmt.class).size();
        count += callable.findAll(ForEachStmt.class).size();
        count += callable.findAll(WhileStmt.class).size();
        count += callable.findAll(DoStmt.class).size();
        count += callable.findAll(CatchClause.class).size();
        count += callable.findAll(ConditionalExpr.class).size();
        count += (int) callable.findAll(SwitchEntry.class).stream()
                .filter(entry -> !entry.getLabels().isEmpty())
                .count();
        count += (int) callable.findAll(BinaryExpr.class).stream()
                .filter(expr -> expr.getOperator() == BinaryExpr.Operator.AND
                        || expr.getOperator() == BinaryExpr.Operator.OR)
                .count();
        return count;
    }
}
