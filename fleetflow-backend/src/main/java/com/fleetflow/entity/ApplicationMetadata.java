package com.fleetflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "application_metadata")
public class ApplicationMetadata extends BaseEntity {

    @Column(name = "application_name", nullable = false, length = 100)
    private String applicationName;

    @Column(name = "environment", nullable = false, length = 30)
    private String environment;

    protected ApplicationMetadata() {
    }

    public ApplicationMetadata(
            String applicationName,
            String environment
    ) {
        this.applicationName = applicationName;
        this.environment = environment;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getEnvironment() {
        return environment;
    }
}