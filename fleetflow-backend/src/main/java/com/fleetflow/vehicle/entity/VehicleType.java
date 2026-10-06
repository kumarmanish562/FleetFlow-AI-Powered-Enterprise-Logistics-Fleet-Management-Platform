package com.fleetflow.vehicle.entity;

import com.fleetflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "vehicle_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vehicle_types_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleType extends BaseEntity {

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(length = 255)
    private String description;

    @Column(
            name = "max_load_kg",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal maxLoadKg;
}