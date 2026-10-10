package com.fleetflow.route.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.route.enums.RouteStatus;
import com.fleetflow.shipment.entity.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_routes_number",
                        columnNames = "route_number"
                ),
                @UniqueConstraint(
                        name = "uk_routes_shipment",
                        columnNames = "shipment_id"
                )
        },
        indexes = {
                @Index(name = "idx_routes_status", columnList = "status"),
                @Index(
                        name = "idx_routes_planned_departure",
                        columnList = "planned_departure_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route extends BaseEntity {

    @Column(name = "route_number", nullable = false, length = 50)
    private String routeNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "shipment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_routes_shipment")
    )
    private Shipment shipment;

    @Column(name = "origin_address", nullable = false, columnDefinition = "TEXT")
    private String originAddress;

    @Column(name = "destination_address", nullable = false, columnDefinition = "TEXT")
    private String destinationAddress;

    @Column(name = "distance_km", precision = 12, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "planned_departure_at")
    private OffsetDateTime plannedDepartureAt;

    @Column(name = "estimated_arrival_at")
    private OffsetDateTime estimatedArrivalAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RouteStatus status;
}