package com.fleetflow.analytics.service;

import com.fleetflow.analytics.dto.AnalyticsSummaryResponse;
import com.fleetflow.analytics.dto.ChartDataResponse;
import com.fleetflow.analytics.dto.DateRangeRequest;
import com.fleetflow.analytics.dto.DriverPerformanceResponse;
import com.fleetflow.analytics.dto.MaintenanceScheduleResponse;
import com.fleetflow.analytics.repository.AnalyticsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {
    private final AnalyticsRepository repository;
    private final String maintenanceCurrency;
    private final Clock clock;

    public AnalyticsService(AnalyticsRepository repository,
            @Value("${fleetflow.billing.currency:INR}") String maintenanceCurrency,
            Clock clock) {
        this.repository = repository;
        this.maintenanceCurrency = maintenanceCurrency;
        this.clock = clock;
    }

    public AnalyticsSummaryResponse dashboard(DateRangeRequest range) {
        Objects.requireNonNull(range, "range");
        validateFilterScope(range, false, false, false, false, false);
        return new AnalyticsSummaryResponse(
                repository.fleet(range.from(),range.to()),
                repository.shipments(range.from(),range.to()),
                repository.drivers(),
                repository.financial(range),
                repository.delivery(range),
                repository.maintenance(range,maintenanceCurrency));
    }

    public AnalyticsSummaryResponse.Fleet fleet(DateRangeRequest range) {
        validateFilterScope(range, false, true, false, false, false);
        return repository.fleet(range);
    }
    public AnalyticsSummaryResponse.Shipments shipments(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.shipments(range);
    }
    public AnalyticsSummaryResponse.Drivers drivers() { return repository.drivers(); }
    public ChartDataResponse driverAvailabilityDistribution() {
        return repository.driverAvailabilityDistribution();
    }
    public Page<DriverPerformanceResponse> driverPerformance(DateRangeRequest range, Pageable pageable) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.driverPerformance(range,pageable);
    }
    public Page<MaintenanceScheduleResponse> upcomingMaintenance(DateRangeRequest filters, Pageable pageable) {
        validateFilterScope(filters, false, true, true, false, false);
        return repository.upcomingMaintenance(java.time.LocalDate.now(clock),filters,pageable);
    }
    public AnalyticsSummaryResponse.Financial financial(DateRangeRequest range) {
        validateFilterScope(range, false, false, false, true, true);
        return repository.financial(range);
    }
    public AnalyticsSummaryResponse.Delivery delivery(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.delivery(range);
    }
    public AnalyticsSummaryResponse.Maintenance maintenance(DateRangeRequest range) {
        validateFilterScope(range, false, true, true, false, false);
        return repository.maintenance(range,maintenanceCurrency);
    }
    public List<ChartDataResponse> shipmentCharts(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, true);
        return repository.shipmentCharts(range);
    }
    public ChartDataResponse shipmentStatusChart(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.shipmentStatusChart(range);
    }
    public ChartDataResponse shipmentTrendChart(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, true);
        return repository.shipmentTrendChart(range);
    }
    public List<ChartDataResponse> fleetCharts(DateRangeRequest range) {
        var charts=new java.util.ArrayList<>(repository.fleetCharts(range.from(),range.to()));
        charts.set(2,withMaintenanceCurrency(charts.get(2)));
        charts.add(withMaintenanceCurrency(repository.maintenanceCostByVehicle(range.from(),range.to())));
        charts.add(repository.vehicleShipmentVolume(range.from(),range.to()));
        return List.copyOf(charts);
    }
    public List<ChartDataResponse> financialCharts(DateRangeRequest range) {
        validateFilterScope(range, false, false, false, true, true);
        return repository.revenueTrends(range);
    }
    public ChartDataResponse paymentStatusChart(DateRangeRequest range) {
        validateFilterScope(range, false, false, false, true, false);
        return repository.paymentStatusChart(range);
    }
    public ChartDataResponse customerRevenue(DateRangeRequest range) {
        validateFilterScope(range, false, false, false, true, false);
        return repository.customerRevenue(range);
    }
    public ChartDataResponse shipmentRevenue(DateRangeRequest range) {
        validateFilterScope(range, false, false, false, true, false);
        return repository.revenueByBillingShipment(range);
    }
    public ChartDataResponse paymentMethodDistribution(DateRangeRequest range) {
        validateFilterScope(range, false, false, false, true, false);
        return repository.paymentMethodDistribution(range);
    }
    public ChartDataResponse maintenanceExpenses(DateRangeRequest range) {
        validateFilterScope(range, false, true, true, false, false);
        return withMaintenanceCurrency(repository.maintenanceCostChart(range));
    }
    public ChartDataResponse maintenanceCostTrend(DateRangeRequest range) {
        validateFilterScope(range, false, true, true, false, false);
        return withMaintenanceCurrency(repository.maintenanceCostChart(range));
    }
    public ChartDataResponse maintenanceTypeDistribution(DateRangeRequest range) {
        validateFilterScope(range, false, true, true, false, false);
        return repository.maintenanceTypeDistribution(range);
    }
    public ChartDataResponse maintenanceFrequencyByVehicle(DateRangeRequest range) {
        validateFilterScope(range, false, true, true, false, false);
        return repository.maintenanceFrequencyByVehicle(range);
    }
    public ChartDataResponse vehicleStatusChart() {
        return repository.vehicleStatusChart();
    }
    public ChartDataResponse vehicleShipmentVolume(DateRangeRequest range) {
        validateFilterScope(range, false, true, false, false, false);
        return repository.vehicleShipmentVolume(range);
    }
    public List<ChartDataResponse> deliveryCharts(DateRangeRequest range) {
        var delivery=delivery(range);
        return List.of(new ChartDataResponse("delivery-outcome",List.of(
                new ChartDataResponse.Point("DELIVERED",BigDecimal.valueOf(delivery.successful())),
                new ChartDataResponse.Point("FAILED",BigDecimal.valueOf(delivery.failed())))));
    }
    public ChartDataResponse deliveryOnTimeChart(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.deliveryOnTimeChart(range);
    }
    public ChartDataResponse deliveryByRoute(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.deliveryByRoute(range);
    }

    public List<AnalyticsRepository.RouteDelayMetric> routeDelaysForAI(LocalDate from, LocalDate to, UUID organizationId) {
        validateAiDateRange(from, to);
        return repository.routeDelaysForAI(from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC), organizationId);
    }

    public AnalyticsSummaryResponse.Fleet fleetForAI(UUID organizationId) {
        return repository.fleetForAI(Objects.requireNonNull(organizationId, "organizationId"));
    }

    public AnalyticsSummaryResponse.Delivery deliveryPerformanceForAI(LocalDate from, LocalDate to,
                                                                       UUID organizationId) {
        validateAiDateRange(from, to);
        return repository.deliveryPerformanceForAI(from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                Objects.requireNonNull(organizationId, "organizationId"));
    }

    private static void validateAiDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > 89)
            throw new com.fleetflow.common.exception.BadRequestException(
                    "Analytics range must be ordered and no longer than 90 days");
    }
    public ChartDataResponse deliveryByCustomer(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.deliveryByCustomer(range);
    }
    public ChartDataResponse driverPerformanceChart(DateRangeRequest range) {
        validateFilterScope(range, true, true, false, false, false);
        return repository.driverDeliveryPerformance(range);
    }

    private static void validateFilterScope(DateRangeRequest f, boolean shipmentFilters,
            boolean vehicleFilter, boolean maintenanceTypeFilter, boolean paymentStatusFilter,
            boolean timeBucket) {
        if (f==null) return;
        boolean unsupported = (!shipmentFilters && (f.status()!=null || f.driverId()!=null || f.customerId()!=null || f.routeId()!=null))
                || (!maintenanceTypeFilter && f.maintenanceType()!=null)
                || (!paymentStatusFilter && f.paymentStatus()!=null)
                || (!vehicleFilter && f.vehicleId()!=null)
                || (!timeBucket && f.bucket()!=null && f.bucket()!=com.fleetflow.analytics.enums.AnalyticsTimeBucket.DAILY);
        if (unsupported) throw new com.fleetflow.common.exception.BadRequestException("One or more filters are not supported by this analytics report");
    }

    public static BigDecimal percentage(long numerator,long denominator) {
        return denominator==0?BigDecimal.ZERO:BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(denominator),2,RoundingMode.HALF_UP);
    }

    private ChartDataResponse withMaintenanceCurrency(ChartDataResponse chart) {
        return new ChartDataResponse(chart.key()+"-"+maintenanceCurrency,chart.points());
    }
}
