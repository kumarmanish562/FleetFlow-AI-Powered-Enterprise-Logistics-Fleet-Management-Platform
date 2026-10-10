package com.fleetflow.delivery.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.delivery.enums.DeliveryStatus;
import com.fleetflow.shipment.entity.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "deliveries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_deliveries_shipment",
                        columnNames = "shipment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_deliveries_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_deliveries_scheduled",
                        columnList = "scheduled_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "shipment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_deliveries_shipment")
    )
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "proof_of_delivery_url", columnDefinition = "TEXT")
    private String proofOfDeliveryUrl;
}