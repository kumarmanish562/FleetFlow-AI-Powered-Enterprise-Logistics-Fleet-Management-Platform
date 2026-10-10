
package com.fleetflow.shipment.entity;

import com.fleetflow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "shipment_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_weight_kg", precision = 12, scale = 2)
    private BigDecimal unitWeightKg;

    @Column(name = "unit_price", precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Builder
    private ShipmentItem(
            Shipment shipment,
            String description,
            Integer quantity,
            BigDecimal unitWeightKg,
            BigDecimal unitPrice
    ) {
        this.shipment = shipment;
        setDescription(description);
        setQuantity(quantity);
        setUnitWeightKg(unitWeightKg);
        setUnitPrice(unitPrice);
    }

    void attachToShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Item description is required"
            );
        }

        String normalized = description.trim();

        if (normalized.length() > 255) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 255 characters"
            );
        }

        this.description = normalized;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        this.quantity = quantity;
    }

    public void setUnitWeightKg(BigDecimal unitWeightKg) {
        if (unitWeightKg != null && unitWeightKg.signum() < 0) {
            throw new IllegalArgumentException(
                    "Unit weight cannot be negative"
            );
        }

        this.unitWeightKg = unitWeightKg;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice != null && unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Unit price cannot be negative"
            );
        }

        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalWeightKg() {
        return unitWeightKg == null
                ? null
                : unitWeightKg.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    public BigDecimal getTotalPrice() {
        return unitPrice == null
                ? null
                : unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }
}
