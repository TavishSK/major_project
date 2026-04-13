package com.major.cloud.service;

import com.major.cloud.strategy.ScalingStrategy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScalingService {

    private final Map<String, ScalingStrategy> strategies;

    public ScalingService(Map<String, ScalingStrategy> strategies) {
        this.strategies = strategies;
    }

    public int applyStrategy(String strategyType, int cpu, int replicas) {
        ScalingStrategy strategy = strategies.get(strategyType);
        if (strategy == null) return replicas;
        return strategy.scale(cpu, replicas);
    }
}