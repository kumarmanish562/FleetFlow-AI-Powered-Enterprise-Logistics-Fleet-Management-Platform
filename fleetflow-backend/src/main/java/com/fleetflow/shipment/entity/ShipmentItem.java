package com.fleetflow.shipment.entity;

import com.fleetflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "shipment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentItem extends BaseEntity {

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "shipment_id",
            nullable = false
    )
    private Shipment shipment;

    @Column(
            nullable = false,
            length = 255
    )
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(
            name = "unit_weight_kg",
            precision = 12,
            scale = 2
    )
    private BigDecimal unitWeightKg;

    @Column(
            name = "unit_price",
            precision = 14,
            scale = 2
    )
    private BigDecimal unitPrice;
}