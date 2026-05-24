package com.analyzer.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Coupling Between Objects (CBO): the number of distinct external types a file references,
 * excluding the types it declares itself and ubiquitous {@code java.lang} types.
 */
@Component
public class CouplingExtractor implements MetricExtractor {

    private static final Set<String> JAVA_LANG_BASICS = Set.of(
            "String", "Object", "Integer", "Long", "Double", "Float", "Boolean",
            "Byte", "Short", "Character", "Number", "CharSequence", "Math", "System",
            "Void", "Exception", "RuntimeException", "Throwable", "Error", "Class",
            "Comparable", "Runnable", "Iterable", "Thread", "StringBuilder", "Enum");

    @Override
    public String getMetricName() {
        return "coupling";
    }

    @Override
    public FileMetricResult extract(CompilationUnit ast, Path filePath) {
        Set<String> declared = new HashSet<>();
        for (TypeDeclaration<?> type : ast.findAll(TypeDeclaration.class)) {
            declared.add(type.getNameAsString());
        }

        Set<String> referenced = new HashSet<>();
        for (ClassOrInterfaceType type : ast.findAll(ClassOrInterfaceType.class)) {
            String name = type.getNameAsString();
            if (!declared.contains(name) && !JAVA_LANG_BASICS.contains(name)) {
                referenced.add(name);
            }
        }

        FileMetricResult result = new FileMetricResult(filePath.toString());
        result.setCoupling(referenced.size());
        return result;
    }
}
