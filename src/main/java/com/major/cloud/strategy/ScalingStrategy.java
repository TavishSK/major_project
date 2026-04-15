package com.major.cloud.strategy;

public interface ScalingStrategy {
    int scale(int currentReplicas, double metric);
    String getName();
}