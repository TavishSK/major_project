package com.major.cloud.service;

import com.major.cloud.dto.ServiceRequestDTO;
import com.major.cloud.dto.ServiceResponseDTO;
import com.major.cloud.exception.ResourceNotFoundException;
import com.major.cloud.model.ServiceEntity;
import com.major.cloud.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public ServiceResponseDTO createService(ServiceRequestDTO request) {

        ServiceEntity entity = new ServiceEntity();

        entity.setServiceName(request.getServiceName());
        entity.setDockerImage(request.getDockerImage());
        entity.setMinReplicas(request.getMinReplicas());
        entity.setMaxReplicas(request.getMaxReplicas());
        entity.setStatus("RUNNING");

        entity.setCurrentReplicas(1);
        entity.setCpuUsage(0);
        entity.setResponseTime(0.0);
        entity.setScalingEvents(0);
        entity.setStrategy(request.getStrategy() != null ? request.getStrategy() : "CPU");

        return mapToResponse(serviceRepository.save(entity));
    }

    public List<ServiceResponseDTO> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServiceResponseDTO getServiceById(Long id) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        return mapToResponse(entity);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    private ServiceResponseDTO mapToResponse(ServiceEntity entity) {

        ServiceResponseDTO dto = new ServiceResponseDTO();

        dto.setId(entity.getId());
        dto.setServiceName(entity.getServiceName());
        dto.setDockerImage(entity.getDockerImage());
        dto.setMinReplicas(entity.getMinReplicas());
        dto.setMaxReplicas(entity.getMaxReplicas());
        dto.setStatus(entity.getStatus());

        dto.setCurrentReplicas(entity.getCurrentReplicas());
        dto.setCpuUsage(entity.getCpuUsage());
        dto.setResponseTime(entity.getResponseTime());
        dto.setScalingEvents(entity.getScalingEvents());
        dto.setStrategy(entity.getStrategy());

        return dto;
    }
}