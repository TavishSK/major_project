package com.major.cloud.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class MonitoringService {

    private final Random random = new Random();

    public int generateCpuUsage() {
        return random.nextInt(100);
    }

    public double generateResponseTime() {
        return 50 + (200 * random.nextDouble());
    }
}