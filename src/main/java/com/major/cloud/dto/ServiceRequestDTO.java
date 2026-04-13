package com.major.cloud.dto;

public class ServiceRequestDTO {

    private String serviceName;
    private String dockerImage;
    private Integer minReplicas;
    private Integer maxReplicas;
    private String strategy;

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDockerImage() { return dockerImage; }
    public void setDockerImage(String dockerImage) { this.dockerImage = dockerImage; }

    public Integer getMinReplicas() { return minReplicas; }
    public void setMinReplicas(Integer minReplicas) { this.minReplicas = minReplicas; }

    public Integer getMaxReplicas() { return maxReplicas; }
    public void setMaxReplicas(Integer maxReplicas) { this.maxReplicas = maxReplicas; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
}