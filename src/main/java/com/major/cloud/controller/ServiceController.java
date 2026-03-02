package com.major.cloud.controller;

import com.major.cloud.dto.ServiceRequestDTO;
import com.major.cloud.dto.ServiceResponseDTO;
import com.major.cloud.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    public ServiceResponseDTO create(@RequestBody ServiceRequestDTO request) {
        return serviceService.createService(request);
    }

    @GetMapping
    public List<ServiceResponseDTO> getAll() {
        return serviceService.getAllServices();
    }

    @GetMapping("/{id}")
    public ServiceResponseDTO getById(@PathVariable Long id) {
        return serviceService.getServiceById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serviceService.deleteService(id);
    }
}