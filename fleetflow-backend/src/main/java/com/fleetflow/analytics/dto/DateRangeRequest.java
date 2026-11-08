package com.fleetflow.analytics.dto;

import java.time.LocalDate;
import java.util.UUID;
import com.fleetflow.analytics.enums.AnalyticsTimeBucket;
import com.fleetflow.billing.enums.PaymentStatus;
import com.fleetflow.common.exception.BadRequestException;
import com.fleetflow.maintenance.enums.MaintenanceType;
import com.fleetflow.shipment.enums.ShipmentStatus;

public record DateRangeRequest(
        LocalDate from,
        LocalDate to,
        ShipmentStatus status,
        UUID vehicleId,
        UUID driverId,
        UUID customerId,
        UUID routeId,
        MaintenanceType maintenanceType,
        PaymentStatus paymentStatus,
        AnalyticsTimeBucket bucket
) {
    public DateRangeRequest(LocalDate from, LocalDate to) {
        this(from, to, null, null, null, null, null, null, null, AnalyticsTimeBucket.DAILY);
    }

    public DateRangeRequest {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("from must be on or before to");
        }
        if (bucket == null) bucket = AnalyticsTimeBucket.DAILY;
    }
}
