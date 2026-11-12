package com.fleetflow.vehicle.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vehicles_registration",
                        columnNames = "registration_number"
                ),
                @UniqueConstraint(
                        name = "uk_vehicles_vin",
                        columnNames = "vin"
                )
        },
        indexes = {
                @Index(
                        name = "idx_vehicles_type",
                        columnList = "vehicle_type_id"
                ),
                @Index(
                        name = "idx_vehicles_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Column(name = "organization_id", nullable = false, updatable = false)
    @Builder.Default
    private UUID organizationId = com.fleetflow.user.entity.User.DEFAULT_ORGANIZATION_ID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "vehicle_type_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_vehicles_type"
            )
    )
    private VehicleType vehicleType;

    @Column(
            name = "registration_number",
            nullable = false,
            length = 50
    )
    private String registrationNumber;

    @Column(
            length = 100
    )
    private String vin;

    @Column(
            nullable = false,
            length = 100
    )
    private String manufacturer;

    @Column(
            nullable = false,
            length = 100
    )
    private String model;

    @Column(
            name = "manufacture_year"
    )
    private Integer manufactureYear;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "fuel_type",
            nullable = false,
            length = 30
    )
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private VehicleStatus status;

    @Column(
            name = "current_odometer",
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal currentOdometer = BigDecimal.ZERO;
}
