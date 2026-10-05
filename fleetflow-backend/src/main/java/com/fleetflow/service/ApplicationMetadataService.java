package com.fleetflow.service;

import com.fleetflow.entity.ApplicationMetadata;
import com.fleetflow.repository.ApplicationMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationMetadataService {

    private final ApplicationMetadataRepository repository;

    public ApplicationMetadataService(
            ApplicationMetadataRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ApplicationMetadata getMetadata() {
        return repository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();
    }
}