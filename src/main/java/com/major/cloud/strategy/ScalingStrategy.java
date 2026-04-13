package com.major.cloud.strategy;

public interface ScalingStrategy {
    int scale(int cpu, int currentReplicas);
}