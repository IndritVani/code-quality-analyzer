package com.analyzer.analyzer.metrics;

/**
 * Per-file metric accumulator. Each {@link MetricExtractor} returns an instance with only
 * the fields it owns populated; the engine merges the partial results for a file into one.
 * Fields are boxed so {@code null} unambiguously means "this extractor did not set it".
 */
public class FileMetricResult {

    private final String filePath;

    private Integer loc;
    private Integer effectiveLoc;
    private Double avgCyclomatic;
    private Double maxCyclomatic;
    private Integer coupling;
    private Double commentDensity;
    private Double volume;

    public FileMetricResult(String filePath) {
        this.filePath = filePath;
    }

    /** Fill any field not yet set on this result from {@code other}. */
    public void merge(FileMetricResult other) {
        if (other == null) {
            return;
        }
        if (loc == null) loc = other.loc;
        if (effectiveLoc == null) effectiveLoc = other.effectiveLoc;
        if (avgCyclomatic == null) avgCyclomatic = other.avgCyclomatic;
        if (maxCyclomatic == null) maxCyclomatic = other.maxCyclomatic;
        if (coupling == null) coupling = other.coupling;
        if (commentDensity == null) commentDensity = other.commentDensity;
        if (volume == null) volume = other.volume;
    }

    public String getFilePath() {
        return filePath;
    }

    public int loc() {
        return loc == null ? 0 : loc;
    }

    public int effectiveLoc() {
        return effectiveLoc == null ? 0 : effectiveLoc;
    }

    public double avgCyclomatic() {
        return avgCyclomatic == null ? 0.0 : avgCyclomatic;
    }

    public double maxCyclomatic() {
        return maxCyclomatic == null ? 0.0 : maxCyclomatic;
    }

    public int coupling() {
        return coupling == null ? 0 : coupling;
    }

    public double commentDensity() {
        return commentDensity == null ? 0.0 : commentDensity;
    }

    public double volume() {
        return volume == null ? 0.0 : volume;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public void setEffectiveLoc(int effectiveLoc) {
        this.effectiveLoc = effectiveLoc;
    }

    public void setAvgCyclomatic(double avgCyclomatic) {
        this.avgCyclomatic = avgCyclomatic;
    }

    public void setMaxCyclomatic(double maxCyclomatic) {
        this.maxCyclomatic = maxCyclomatic;
    }

    public void setCoupling(int coupling) {
        this.coupling = coupling;
    }

    public void setCommentDensity(double commentDensity) {
        this.commentDensity = commentDensity;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
