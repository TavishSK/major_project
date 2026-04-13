package com.major.cloud.strategy;

import org.springframework.stereotype.Component;

@Component("CPU")
public class CpuStrategy implements ScalingStrategy {

    @Override
    public int scale(int cpu, int replicas) {
        if (cpu > 70) return replicas + 1;
        if (cpu < 30 && replicas > 1) return replicas - 1;
        return replicas;
    }
}