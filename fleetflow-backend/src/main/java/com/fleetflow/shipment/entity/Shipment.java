package com.fleetflow.shipment.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.customer.entity.Customer;
import com.fleetflow.driver.entity.Driver;
import com.fleetflow.route.entity.Route;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.service.ShipmentLifecycle;
import com.fleetflow.vehicle.entity.Vehicle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments", uniqueConstraints =
@UniqueConstraint(name = "uk_shipments_number", columnNames = "shipment_number"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
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

    @Column(name = "shipment_number", nullable = false, length = 50, updatable = false)
    private String shipmentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentPriority priority;

    @Column(name = "origin_address", nullable = false, columnDefinition = "TEXT")
    private String originAddress;

    @Column(name = "destination_address", nullable = false, columnDefinition = "TEXT")
    private String destinationAddress;

    @Column(name = "total_weight_kg", precision = 12, scale = 2)
    private BigDecimal totalWeightKg;

    @Column(name = "declared_value", precision = 14, scale = 2)
    private BigDecimal declaredValue;

    @Column(name = "pickup_date")
    private OffsetDateTime pickupDate;

    @Column(name = "expected_delivery_date")
    private OffsetDateTime expectedDeliveryDate;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items = new ArrayList<>();

    @Builder
    private Shipment(Customer customer, Driver driver, Vehicle vehicle, Route route,
                     String shipmentNumber, ShipmentStatus status, ShipmentPriority priority,
                     String originAddress, String destinationAddress, BigDecimal totalWeightKg,
                     BigDecimal declaredValue, OffsetDateTime pickupDate,
                     OffsetDateTime expectedDeliveryDate, OffsetDateTime deliveredAt,
                     String failureReason, List<ShipmentItem> items) {
        this.customer = customer;
        this.driver = driver;
        this.vehicle = vehicle;
        this.route = route;
        this.shipmentNumber = shipmentNumber;
        this.status = status;
        this.priority = priority;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.totalWeightKg = totalWeightKg;
        this.declaredValue = declaredValue;
        this.pickupDate = pickupDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.deliveredAt = deliveredAt;
        this.failureReason = failureReason;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        this.items.forEach(item -> item.attachToShipment(this));
    }

    public void changeStatus(ShipmentStatus newStatus) {
        ShipmentLifecycle.validateTransition(this.status, newStatus);
        this.status = newStatus;
    }

    public void assignTo(Driver driver, Vehicle vehicle) {
        if (this.status != ShipmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed shipments can be assigned");
        }
        if (driver == null || vehicle == null) {
            throw new IllegalArgumentException("Driver and vehicle are required");
        }
        this.driver = driver;
        this.vehicle = vehicle;
        changeStatus(ShipmentStatus.PICKUP_SCHEDULED);
    }

    public void markPickedUp(OffsetDateTime pickupTime) {
        if (driver == null || vehicle == null) {
            throw new IllegalStateException("Driver and vehicle must be assigned before pickup");
        }
        if (pickupTime == null) {
            throw new IllegalArgumentException("Pickup timestamp is required");
        }
        changeStatus(ShipmentStatus.PICKED_UP);
        this.pickupDate = pickupTime;
    }

    public void markDelivered(OffsetDateTime deliveryTime) {
        if (deliveryTime == null) {
            throw new IllegalArgumentException("Delivery timestamp is required");
        }
        changeStatus(ShipmentStatus.DELIVERED);
        this.deliveredAt = deliveryTime;
        this.failureReason = null;
    }

    public void recordFailure(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Failure reason is required");
        }
        String normalized = reason.trim();
        if (normalized.length() > 1000) {
            throw new IllegalArgumentException("Failure reason cannot exceed 1000 characters");
        }
        changeStatus(ShipmentStatus.FAILED);
        this.failureReason = normalized;
    }

    public void cancel() {
        changeStatus(ShipmentStatus.CANCELLED);
        this.driver = null;
        this.vehicle = null;
    }

    public void addItem(ShipmentItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Shipment item is required");
        }
        items.add(item);
        item.attachToShipment(this);
    }

    public void removeItem(ShipmentItem item) {
        if (item != null && items.remove(item)) {
            item.attachToShipment(null);
        }
    }

    public void changePriority(ShipmentPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Shipment priority is required");
        }
        if (status == ShipmentStatus.DELIVERED || status == ShipmentStatus.CANCELLED
                || status == ShipmentStatus.RETURNED) {
            throw new IllegalStateException("Cannot change priority of a terminal shipment");
        }
        this.priority = priority;
    }


    public void assignRoute(Route route) {

        if (route == null) {
            throw new IllegalArgumentException(
                    "Route is required"
            );
        }

        if (this.status != ShipmentStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only confirmed shipments can be assigned to a route"
            );
        }

        if (this.route != null) {
            throw new IllegalStateException(
                    "Shipment is already assigned to a route"
            );
        }

        this.route = route;
    }

}
