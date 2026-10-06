package com.fleetflow.shipment.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.customer.entity.Customer;
import com.fleetflow.driver.entity.Driver;
import com.fleetflow.route.entity.Route;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.vehicle.entity.Vehicle;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "shipments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shipments_number",
                        columnNames = "shipment_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(
            name = "shipment_number",
            nullable = false,
            length = 50
    )
    private String shipmentNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private ShipmentPriority priority;

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
            name = "total_weight_kg",
            precision = 12,
            scale = 2
    )
    private BigDecimal totalWeightKg;

    @Column(
            name = "declared_value",
            precision = 14,
            scale = 2
    )
    private BigDecimal declaredValue;

    @Column(name = "pickup_date")
    private OffsetDateTime pickupDate;

    @Column(name = "expected_delivery_date")
    private OffsetDateTime expectedDeliveryDate;

    @OneToMany(
            mappedBy = "shipment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ShipmentItem> items =
            new ArrayList<>();
}