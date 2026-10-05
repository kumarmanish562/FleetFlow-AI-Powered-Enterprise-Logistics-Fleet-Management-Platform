package com.fleetflow.controller;

import com.fleetflow.dto.ApplicationMetadataResponse;
import com.fleetflow.mapper.ApplicationMetadataMapper;
import com.fleetflow.service.ApplicationMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class HealthController {

    private final ApplicationMetadataService service;
    private final ApplicationMetadataMapper mapper;

    public HealthController(
            ApplicationMetadataService service,
            ApplicationMetadataMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/metadata")
    public ApplicationMetadataResponse getMetadata() {

        return mapper.toResponse(
                service.getMetadata()
        );
    }
}