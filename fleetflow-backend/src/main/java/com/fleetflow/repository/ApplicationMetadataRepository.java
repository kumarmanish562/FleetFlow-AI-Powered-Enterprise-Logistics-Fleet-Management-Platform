package com.fleetflow.repository;

import com.fleetflow.entity.ApplicationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationMetadataRepository
        extends JpaRepository<ApplicationMetadata, Long> {
}