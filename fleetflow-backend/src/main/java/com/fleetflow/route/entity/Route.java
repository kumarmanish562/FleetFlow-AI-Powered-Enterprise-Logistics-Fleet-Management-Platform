package com.fleetflow.route.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.route.enums.RouteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_routes_number",
                        columnNames = "route_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route extends BaseEntity {

    @Column(
            name = "route_number",
            nullable = false,
            length = 50
    )
    private String routeNumber;

    @Column(
            name = "origin_address",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String originAddress;

    @Column(
            name = "destination_address",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String destinationAddress;

    @Column(
            name = "distance_km",
            precision = 12,
            scale = 2
    )
    private BigDecimal distanceKm;

    @Column(
            name = "estimated_duration_minutes"
    )
    private Integer estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private RouteStatus status;
}