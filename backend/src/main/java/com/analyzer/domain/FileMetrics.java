package com.analyzer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "file_metrics")
public class FileMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private AnalysisRun analysisRun;

    @Column(nullable = false)
    private String filePath;

    private int loc;
    private double avgCyclomatic;
    private double maxCyclomatic;
    private int coupling;
    private double commentDensity;

    protected FileMetrics() {
    }

    public FileMetrics(AnalysisRun analysisRun, String filePath) {
        this.analysisRun = analysisRun;
        this.filePath = filePath;
    }

    public UUID getId() {
        return id;
    }

    public AnalysisRun getAnalysisRun() {
        return analysisRun;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public double getAvgCyclomatic() {
        return avgCyclomatic;
    }

    public void setAvgCyclomatic(double avgCyclomatic) {
        this.avgCyclomatic = avgCyclomatic;
    }

    public double getMaxCyclomatic() {
        return maxCyclomatic;
    }

    public void setMaxCyclomatic(double maxCyclomatic) {
        this.maxCyclomatic = maxCyclomatic;
    }

    public int getCoupling() {
        return coupling;
    }

    public void setCoupling(int coupling) {
        this.coupling = coupling;
    }

    public double getCommentDensity() {
        return commentDensity;
    }

    public void setCommentDensity(double commentDensity) {
        this.commentDensity = commentDensity;
    }
}
