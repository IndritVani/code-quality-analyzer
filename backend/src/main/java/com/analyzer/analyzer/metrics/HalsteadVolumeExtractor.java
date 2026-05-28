package com.analyzer.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Halstead Volume per file: {@code V = N * log2(n)}, where {@code N} is the total number of
 * operators + operands and {@code n} is the number of <em>distinct</em> operators + operands.
 *
 * <p>Java has no single canonical operator/operand split, so we use a documented, reproducible
 * heuristic (exactness matters less than consistency, since the score is relative):
 * <ul>
 *   <li><b>operators</b> — binary / unary / assignment operator symbols, the ternary {@code ?:},
 *       method invocations (keyed by name), and the control-flow keywords
 *       if / for / for-each / while / do / case / catch / return / throw.</li>
 *   <li><b>operands</b> — identifier references ({@code NameExpr}) and literals
 *       (numbers, strings, chars, booleans, {@code null}).</li>
 * </ul>
 * An empty or trivial file yields {@code V = 0} (guarded against {@code log(0)}).
 */
@Component
public class HalsteadVolumeExtractor implements MetricExtractor {

    @Override
    public String getMetricName() {
        return "halstead_volume";
    }

    @Override
    public FileMetricResult extract(CompilationUnit ast, Path filePath) {
        Set<String> distinctOperators = new HashSet<>();
        Set<String> distinctOperands = new HashSet<>();
        int totalOperators = 0;
        int totalOperands = 0;

        // --- Operators ---
        for (BinaryExpr e : ast.findAll(BinaryExpr.class)) {
            distinctOperators.add(e.getOperator().asString());
            totalOperators++;
        }
        for (UnaryExpr e : ast.findAll(UnaryExpr.class)) {
            distinctOperators.add(e.getOperator().asString());
            totalOperators++;
        }
        for (AssignExpr e : ast.findAll(AssignExpr.class)) {
            distinctOperators.add(e.getOperator().asString());
            totalOperators++;
        }
        for (MethodCallExpr e : ast.findAll(MethodCallExpr.class)) {
            distinctOperators.add("call:" + e.getNameAsString());
            totalOperators++;
        }
        totalOperators += keyword(ast.findAll(IfStmt.class).size(), "if", distinctOperators);
        totalOperators += keyword(ast.findAll(ForStmt.class).size(), "for", distinctOperators);
        totalOperators += keyword(ast.findAll(ForEachStmt.class).size(), "for-each", distinctOperators);
        totalOperators += keyword(ast.findAll(WhileStmt.class).size(), "while", distinctOperators);
        totalOperators += keyword(ast.findAll(DoStmt.class).size(), "do", distinctOperators);
        totalOperators += keyword(ast.findAll(CatchClause.class).size(), "catch", distinctOperators);
        totalOperators += keyword(ast.findAll(ReturnStmt.class).size(), "return", distinctOperators);
        totalOperators += keyword(ast.findAll(ThrowStmt.class).size(), "throw", distinctOperators);
        totalOperators += keyword(ast.findAll(ConditionalExpr.class).size(), "?:", distinctOperators);
        int cases = (int) ast.findAll(SwitchEntry.class).stream()
                .filter(entry -> !entry.getLabels().isEmpty())
                .count();
        totalOperators += keyword(cases, "case", distinctOperators);

        // --- Operands ---
        for (NameExpr e : ast.findAll(NameExpr.class)) {
            distinctOperands.add(e.getNameAsString());
            totalOperands++;
        }
        for (LiteralExpr e : ast.findAll(LiteralExpr.class)) {
            distinctOperands.add(e.toString());
            totalOperands++;
        }

        int distinct = distinctOperators.size() + distinctOperands.size();
        int total = totalOperators + totalOperands;
        double volume = distinct == 0 ? 0.0 : total * (Math.log(distinct) / Math.log(2));

        FileMetricResult result = new FileMetricResult(filePath.toString());
        result.setVolume(volume);
        return result;
    }

    /** Record the keyword as a distinct operator when present and contribute its occurrence count. */
    private int keyword(int count, String symbol, Set<String> distinctOperators) {
        if (count > 0) {
            distinctOperators.add(symbol);
        }
        return count;
    }
}
