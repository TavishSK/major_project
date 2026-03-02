package com.major.cloud.dto;

import lombok.Data;

@Data
public class ServiceRequestDTO {

    private String serviceName;
    private String dockerImage;
    private Integer minReplicas;
    private Integer maxReplicas;
}
