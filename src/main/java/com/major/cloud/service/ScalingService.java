package com.major.cloud.service;

import com.major.cloud.strategy.ScalingStrategy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class ScalingService {

    private final Map<String, ScalingStrategy> strategies;
    private final Random random = new Random();

    public ScalingService(Map<String, ScalingStrategy> strategies) {
        this.strategies = strategies;
    }

    public int applyStrategy(String strategyType, int currentReplicas) {

        ScalingStrategy strategy = strategies.get(strategyType.toUpperCase());

        if (strategy == null) {
            return currentReplicas;
        }

        // Simulate different metrics based on strategy
        double metric;

        switch (strategyType.toUpperCase()) {
            case "CPU":
                metric = random.nextInt(100); // CPU usage %
                break;

            case "TREND":
                metric = random.nextDouble() * 100; // simulated trend score
                break;

            case "LATENCY":
                metric = 50 + random.nextDouble() * 200; // latency ms (50–250)
                break;

            default:
                metric = random.nextDouble() * 100;
        }

        return strategy.scale(currentReplicas, metric);
    }
}