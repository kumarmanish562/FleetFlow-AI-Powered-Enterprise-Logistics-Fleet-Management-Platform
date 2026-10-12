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
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "maintenance_records",
        indexes = {
                @Index(
                        name = "idx_maintenance_vehicle_date",
                        columnList = "vehicle_id, maintenance_date"
                ),
                @Index(
                        name = "idx_maintenance_status_date",
                        columnList = "status, maintenance_date"
                )
        }
)
public class MaintenanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "vehicle_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_maintenance_records_vehicle"
            )
    )
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false, length = 30)
    private MaintenanceType maintenanceType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @Column(nullable = false)
    private Long mileage;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(name = "safety_critical", nullable = false)
    private boolean safetyCritical = false;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "parts_replaced", columnDefinition = "TEXT")
    private String partsReplaced;

    @Column(name = "downtime_hours", precision = 10, scale = 2)
    private BigDecimal downtimeHours;

    @Version
    @Column(nullable = false)
    private Long version;
}