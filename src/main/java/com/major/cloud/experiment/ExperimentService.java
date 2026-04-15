package com.major.cloud.experiment;

import com.major.cloud.service.MonitoringService;
import com.major.cloud.service.ScalingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExperimentService {

    private final MonitoringService monitoringService;
    private final ScalingService scalingService;

    public ExperimentService(MonitoringService monitoringService,
                             ScalingService scalingService) {
        this.monitoringService = monitoringService;
        this.scalingService = scalingService;
    }

    public List<ExperimentResult> runExperiment(List<String> strategies) {

        List<ExperimentResult> results = new ArrayList<>();

        for (String strategy : strategies) {

            int replicas = 1;
            int scalingEvents = 0;
            double totalResponse = 0;

            for (int i = 0; i < 10; i++) {

                int cpu = monitoringService.generateCpuUsage();
                double response = monitoringService.generateResponseTime();

                int newReplicas = scalingService.applyStrategy(strategy, replicas);

                if (newReplicas != replicas) {
                    scalingEvents++;
                }

                replicas = newReplicas;
                totalResponse += response;
            }

            double avgResponse = totalResponse / 10;

            results.add(new ExperimentResult(
                    strategy,
                    replicas,
                    avgResponse,
                    scalingEvents
            ));
        }

        return results;
    }

    public String findBestStrategy(List<ExperimentResult> results) {

        return results.stream()
                .min((a, b) -> Double.compare(a.getAvgResponseTime(), b.getAvgResponseTime()))
                .map(ExperimentResult::getStrategy)
                .orElse("NONE");
    }
}