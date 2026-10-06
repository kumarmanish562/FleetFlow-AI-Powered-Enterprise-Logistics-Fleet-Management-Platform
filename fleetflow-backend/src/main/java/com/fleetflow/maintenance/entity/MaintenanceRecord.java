package com.fleetflow.maintenance.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.maintenance.enums.MaintenanceStatus;
import com.fleetflow.maintenance.enums.MaintenanceType;
import com.fleetflow.vehicle.entity.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord extends BaseEntity {

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "vehicle_id",
            nullable = false
    )
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "maintenance_type",
            nullable = false,
            length = 50
    )
    private MaintenanceType maintenanceType;

    @Column(
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "maintenance_date",
            nullable = false
    )
    private LocalDate maintenanceDate;

    @Column(
            name = "odometer_reading",
            precision = 12,
            scale = 2
    )
    private BigDecimal odometerReading;

    @Column(
            precision = 14,
            scale = 2
    )
    private BigDecimal cost;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private MaintenanceStatus status;
}