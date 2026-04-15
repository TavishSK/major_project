package com.major.cloud.strategy;

import org.springframework.stereotype.Component;

@Component("LATENCY")
public class LatencyStrategy implements ScalingStrategy {

    @Override
    public int scale(int currentReplicas, double metric) {

        // metric = latency in ms

        if (metric > 150) {
            return currentReplicas + 2; // aggressive scaling
        } else if (metric < 80) {
            return Math.max(1, currentReplicas - 1);
        }

        return currentReplicas;
    }

    @Override
    public String getName() {
        return "LATENCY";
    }
}