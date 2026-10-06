package com.fleetflow.tracking.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.tracking.enums.TrackingEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.OffsetDateTime;

@Entity
@Table(name = "tracking_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEvent extends BaseEntity {

    @ManyToOne(
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
            name = "event_type",
            nullable = false,
            length = 50
    )
    private TrackingEventType eventType;

    @Column(
            name = "event_time",
            nullable = false
    )
    private OffsetDateTime eventTime;

    @Column(
            precision = 10,
            scale = 7
    )
    private BigDecimal latitude;

    @Column(
            precision = 10,
            scale = 7
    )
    private BigDecimal longitude;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(
            columnDefinition = "TEXT"
    )
    private String description;
}