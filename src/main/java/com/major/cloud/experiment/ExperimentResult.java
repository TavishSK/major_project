package com.major.cloud.experiment;

public class ExperimentResult {

    private String strategy;
    private int finalReplicas;
    private double avgResponseTime;
    private int scalingEvents;

    public ExperimentResult() {}

    public ExperimentResult(String strategy, int finalReplicas, double avgResponseTime, int scalingEvents) {
        this.strategy = strategy;
        this.finalReplicas = finalReplicas;
        this.avgResponseTime = avgResponseTime;
        this.scalingEvents = scalingEvents;
    }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public int getFinalReplicas() { return finalReplicas; }
    public void setFinalReplicas(int finalReplicas) { this.finalReplicas = finalReplicas; }

    public double getAvgResponseTime() { return avgResponseTime; }
    public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }

    public int getScalingEvents() { return scalingEvents; }
    public void setScalingEvents(int scalingEvents) { this.scalingEvents = scalingEvents; }
}