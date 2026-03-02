package com.major.cloud.service;

import com.major.cloud.dto.ServiceRequestDTO;
import com.major.cloud.dto.ServiceResponseDTO;
import com.major.cloud.exception.ResourceNotFoundException;
import com.major.cloud.model.ServiceEntity;
import com.major.cloud.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceResponseDTO createService(ServiceRequestDTO request) {

        ServiceEntity entity = ServiceEntity.builder()
                .serviceName(request.getServiceName())
                .dockerImage(request.getDockerImage())
                .minReplicas(request.getMinReplicas())
                .maxReplicas(request.getMaxReplicas())
                .status("RUNNING")
                .build();

        ServiceEntity saved = serviceRepository.save(entity);

        return mapToResponse(saved);
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
        return ServiceResponseDTO.builder()
                .id(entity.getId())
                .serviceName(entity.getServiceName())
                .dockerImage(entity.getDockerImage())
                .minReplicas(entity.getMinReplicas())
                .maxReplicas(entity.getMaxReplicas())
                .status(entity.getStatus())
                .build();
    }
}
