package com.analyzer.analyzer;

import com.analyzer.analyzer.metrics.FileMetricResult;
import com.analyzer.analyzer.metrics.MetricExtractor;
import com.analyzer.analyzer.pipeline.AnalysisResult;
import com.analyzer.analyzer.pipeline.DependencyHealthAnalyzer;
import com.analyzer.analyzer.pipeline.DuplicationAnalyzer;
import com.analyzer.analyzer.pipeline.FileDiscovery;
import com.analyzer.analyzer.pipeline.ProjectMetricSummary;
import com.analyzer.analyzer.pipeline.ResultAggregator;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Drives the analysis pipeline:
 * {@code FileDiscovery → JavaParser → MetricExtractors[] → project analyzers → ResultAggregator}.
 */
@Component
public class AnalyzerEngine {

    private static final Logger log = LoggerFactory.getLogger(AnalyzerEngine.class);

    private final FileDiscovery fileDiscovery;
    private final List<MetricExtractor> extractors;
    private final DuplicationAnalyzer duplicationAnalyzer;
    private final DependencyHealthAnalyzer dependencyHealthAnalyzer;
    private final ResultAggregator aggregator;

    public AnalyzerEngine(FileDiscovery fileDiscovery,
                          List<MetricExtractor> extractors,
                          DuplicationAnalyzer duplicationAnalyzer,
                          DependencyHealthAnalyzer dependencyHealthAnalyzer,
                          ResultAggregator aggregator) {
        this.fileDiscovery = fileDiscovery;
        this.extractors = extractors;
        this.duplicationAnalyzer = duplicationAnalyzer;
        this.dependencyHealthAnalyzer = dependencyHealthAnalyzer;
        this.aggregator = aggregator;
    }

    public AnalysisResult run(Path projectRoot) {
        // BLEEDING_EDGE is the most permissive level this JavaParser supports; it parses the
        // newest Java syntax (records, sealed types, switch/record patterns) on modern repos.
        JavaParser parser = new JavaParser(
                new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE));

        List<Path> files = fileDiscovery.discover(projectRoot);
        List<FileMetricResult> fileResults = new ArrayList<>(files.size());

        for (Path file : files) {
            CompilationUnit ast = parse(parser, file);
            if (ast == null) {
                continue; // unparseable file — skipped, run continues
            }
            String relativePath = projectRoot.relativize(file).toString().replace('\\', '/');
            FileMetricResult merged = new FileMetricResult(relativePath);
            for (MetricExtractor extractor : extractors) {
                merged.merge(extractor.extract(ast, file));
            }
            fileResults.add(merged);
        }

        double duplicationRatio = duplicationAnalyzer.duplicationRatio(files);
        var dependencyHealth = dependencyHealthAnalyzer.analyze(projectRoot);

        ProjectMetricSummary summary =
                aggregator.aggregate(fileResults, duplicationRatio, dependencyHealth.outdatedDependencies());

        log.info("Analyzed {}: {} files, MI={}", projectRoot, fileResults.size(),
                String.format("%.1f", summary.maintainabilityIndex()));
        return new AnalysisResult(fileResults, summary);
    }

    private CompilationUnit parse(JavaParser parser, Path file) {
        try {
            ParseResult<CompilationUnit> result = parser.parse(file);
            return result.getResult().orElse(null);
        } catch (Exception e) {
            log.warn("Skipping unparseable file {}: {}", file, e.getMessage());
            return null;
        }
    }
}
