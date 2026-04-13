package com.major.cloud.strategy;

import org.springframework.stereotype.Component;

@Component("TREND")
public class TrendStrategy implements ScalingStrategy {

    @Override
    public int scale(int cpu, int replicas) {
        if (cpu > 60) return replicas + 2;
        if (cpu < 40 && replicas > 1) return replicas - 1;
        return replicas;
    }
}