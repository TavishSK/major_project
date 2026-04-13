package com.major.cloud.scheduler;

import com.major.cloud.model.ServiceEntity;
import com.major.cloud.repository.ServiceRepository;
import com.major.cloud.service.MonitoringService;
import com.major.cloud.service.ScalingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MonitoringScheduler {

    private final ServiceRepository repo;
    private final MonitoringService monitoringService;
    private final ScalingService scalingService;

    public MonitoringScheduler(ServiceRepository repo,
                               MonitoringService monitoringService,
                               ScalingService scalingService) {
        this.repo = repo;
        this.monitoringService = monitoringService;
        this.scalingService = scalingService;
    }

    @Scheduled(fixedRate = 5000)
    public void monitorAndScale() {

        List<ServiceEntity> services = repo.findAll();

        for (ServiceEntity service : services) {

            int cpu = monitoringService.generateCpuUsage();
            double responseTime = monitoringService.generateResponseTime();

            if (service.getCurrentReplicas() == null) {
                service.setCurrentReplicas(1);
            }

            int newReplicas = scalingService.applyStrategy(
                    service.getStrategy(),
                    cpu,
                    service.getCurrentReplicas()
            );

            if (newReplicas != service.getCurrentReplicas()) {
                service.setScalingEvents(
                        service.getScalingEvents() == null ? 1 : service.getScalingEvents() + 1
                );
            }

            service.setCpuUsage(cpu);
            service.setResponseTime(responseTime);
            service.setCurrentReplicas(newReplicas);

            repo.save(service);
        }
    }
}