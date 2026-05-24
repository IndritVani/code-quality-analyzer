package com.analyzer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "project_metrics")
public class ProjectMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_run_id", nullable = false, unique = true)
    private AnalysisRun analysisRun;

    private int totalLoc;
    private double avgCyclomatic;
    private double maxCyclomatic;
    private double duplicationRatio;
    private int outdatedDependencies;
    private double avgCoupling;
    private double commentDensity;
    private double maintainabilityIndex;

    protected ProjectMetrics() {
    }

    public ProjectMetrics(AnalysisRun analysisRun) {
        this.analysisRun = analysisRun;
    }

    public UUID getId() {
        return id;
    }

    public AnalysisRun getAnalysisRun() {
        return analysisRun;
    }

    public int getTotalLoc() {
        return totalLoc;
    }

    public void setTotalLoc(int totalLoc) {
        this.totalLoc = totalLoc;
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

    public double getDuplicationRatio() {
        return duplicationRatio;
    }

    public void setDuplicationRatio(double duplicationRatio) {
        this.duplicationRatio = duplicationRatio;
    }

    public int getOutdatedDependencies() {
        return outdatedDependencies;
    }

    public void setOutdatedDependencies(int outdatedDependencies) {
        this.outdatedDependencies = outdatedDependencies;
    }

    public double getAvgCoupling() {
        return avgCoupling;
    }

    public void setAvgCoupling(double avgCoupling) {
        this.avgCoupling = avgCoupling;
    }

    public double getCommentDensity() {
        return commentDensity;
    }

    public void setCommentDensity(double commentDensity) {
        this.commentDensity = commentDensity;
    }

    public double getMaintainabilityIndex() {
        return maintainabilityIndex;
    }

    public void setMaintainabilityIndex(double maintainabilityIndex) {
        this.maintainabilityIndex = maintainabilityIndex;
    }
}
