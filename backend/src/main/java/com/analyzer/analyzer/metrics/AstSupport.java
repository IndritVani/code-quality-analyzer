package com.analyzer.analyzer.metrics;

import com.analyzer.analyzer.SourceFiles;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shared helpers for the per-file extractors. */
final class AstSupport {

    private AstSupport() {
    }

    /** Read physical lines, falling back to Latin-1 if the file isn't valid UTF-8. */
    static List<String> readLines(Path file) {
        return SourceFiles.readLines(file);
    }

    /** Set of 1-based line numbers touched by any comment (line, block, or Javadoc). */
    static Set<Integer> commentLineNumbers(CompilationUnit cu) {
        Set<Integer> lines = new HashSet<>();
        for (Comment comment : cu.getAllComments()) {
            comment.getRange().ifPresent(range -> {
                for (int line = range.begin.line; line <= range.end.line; line++) {
                    lines.add(line);
                }
            });
        }
        return lines;
    }
}
