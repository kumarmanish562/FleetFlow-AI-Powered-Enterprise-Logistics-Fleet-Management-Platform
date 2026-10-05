package com.fleetflow.mapper;

import com.fleetflow.dto.ApplicationMetadataResponse;
import com.fleetflow.entity.ApplicationMetadata;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMetadataMapper {

    public ApplicationMetadataResponse toResponse(
            ApplicationMetadata entity
    ) {
        return new ApplicationMetadataResponse(
                entity.getId(),
                entity.getApplicationName(),
                entity.getEnvironment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}