package com.fleetflow.delivery.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.delivery.enums.DeliveryStatus;
import com.fleetflow.shipment.entity.Shipment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseEntity {

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "shipment_id",
            nullable = false
    )
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private DeliveryStatus status;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(
            name = "delivery_notes",
            columnDefinition = "TEXT"
    )
    private String deliveryNotes;

    @Column(name = "proof_of_delivery_url")
    private String proofOfDeliveryUrl;
}
