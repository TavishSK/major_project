package com.major.cloud.strategy;

import org.springframework.stereotype.Component;

@Component("CPU")
public class CpuStrategy implements ScalingStrategy {

    @Override
    public int scale(int currentReplicas, double metric) {

        // metric = CPU %

        if (metric > 70) {
            return currentReplicas + 1; // scale up
        } else if (metric < 30) {
            return Math.max(1, currentReplicas - 1); // scale down
        }

        return currentReplicas;
    }

    @Override
    public String getName() {
        return "CPU";
    }
}