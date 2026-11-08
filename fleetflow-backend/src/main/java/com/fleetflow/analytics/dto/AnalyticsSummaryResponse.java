package com.fleetflow.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsSummaryResponse(
        Fleet fleet,
        Shipments shipments,
        Drivers drivers,
        Financial financial,
        Delivery delivery,
        Maintenance maintenance
) {
    public record Fleet(long total, long active, long available, long assigned,
                        long maintenance, long inactive,
                        BigDecimal assignmentCoveragePercent) {}
    public record Shipments(long total, long pending, long active,
                            long inTransit, long delivered, long delayed,
                            long cancelled, BigDecimal completionPercent) {}
    public record Drivers(long total, long active, long available,
                          long assigned, long unavailable) {}
    public record CurrencyAmount(String currency, BigDecimal amount) {}
    public record Financial(List<CurrencyAmount> invoiced,
                            List<CurrencyAmount> averageInvoiceValue,
                            List<CurrencyAmount> collected,
                            List<CurrencyAmount> outstanding,
                            List<CurrencyAmount> pendingPayments,
                            long successfulPaymentCount,
                            long failedPaymentCount) {}
    public record Delivery(long total, long successful, long failed,
                           long onTime, long delayed,
                           BigDecimal successPercent,
                           BigDecimal onTimePercent,
                           BigDecimal averageDurationHours,
                           BigDecimal averageEstimatedRouteDistanceKm) {}
    public record Maintenance(long recordCount, long scheduled,
                              long inProgress, long completed,
                              long vehiclesUnderMaintenance,
                              List<CurrencyAmount> cost) {}
}
