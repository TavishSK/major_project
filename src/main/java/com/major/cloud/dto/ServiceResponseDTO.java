package com.major.cloud.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceResponseDTO {

    private Long id;
    private String serviceName;
    private String dockerImage;
    private Integer minReplicas;
    private Integer maxReplicas;
    private String status;
}
