package com.major.cloud.dto;

public class ServiceResponseDTO {

    private Long id;
    private String serviceName;
    private String dockerImage;
    private Integer minReplicas;
    private Integer maxReplicas;
    private String status;

    private Integer currentReplicas;
    private Integer cpuUsage;
    private Double responseTime;
    private Integer scalingEvents;
    private String strategy;

    // GETTERS + SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDockerImage() { return dockerImage; }
    public void setDockerImage(String dockerImage) { this.dockerImage = dockerImage; }

    public Integer getMinReplicas() { return minReplicas; }
    public void setMinReplicas(Integer minReplicas) { this.minReplicas = minReplicas; }

    public Integer getMaxReplicas() { return maxReplicas; }
    public void setMaxReplicas(Integer maxReplicas) { this.maxReplicas = maxReplicas; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCurrentReplicas() { return currentReplicas; }
    public void setCurrentReplicas(Integer currentReplicas) { this.currentReplicas = currentReplicas; }

    public Integer getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Integer cpuUsage) { this.cpuUsage = cpuUsage; }

    public Double getResponseTime() { return responseTime; }
    public void setResponseTime(Double responseTime) { this.responseTime = responseTime; }

    public Integer getScalingEvents() { return scalingEvents; }
    public void setScalingEvents(Integer scalingEvents) { this.scalingEvents = scalingEvents; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
}