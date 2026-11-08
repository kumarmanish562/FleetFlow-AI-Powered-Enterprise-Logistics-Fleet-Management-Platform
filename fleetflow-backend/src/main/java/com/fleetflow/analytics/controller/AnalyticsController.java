package com.fleetflow.analytics.controller;

import com.fleetflow.analytics.dto.AnalyticsSummaryResponse;
import com.fleetflow.analytics.dto.ChartDataResponse;
import com.fleetflow.analytics.dto.DateRangeRequest;
import com.fleetflow.analytics.service.AnalyticsService;
import com.fleetflow.analytics.dto.DriverPerformanceResponse;
import com.fleetflow.analytics.dto.MaintenanceScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) { this.service=service; }

    @GetMapping({"/dashboard","/dashboard/summary"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public AnalyticsSummaryResponse dashboard(@ModelAttribute DateRangeRequest filters) {
        return service.dashboard(filters);
    }

    @GetMapping("/fleet/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public AnalyticsSummaryResponse.Fleet fleet(@ModelAttribute DateRangeRequest filters) {
        return service.fleet(filters);
    }

    @GetMapping("/fleet/utilization")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public AnalyticsSummaryResponse.Fleet utilization(@ModelAttribute DateRangeRequest filters) {
        return service.fleet(filters);
    }

    @GetMapping("/shipments/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public AnalyticsSummaryResponse.Shipments shipments(@ModelAttribute DateRangeRequest filters) {
        return service.shipments(filters);
    }

    @GetMapping("/shipments/status-distribution")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public ChartDataResponse shipmentCharts(@ModelAttribute DateRangeRequest filters) {
        return service.shipmentStatusChart(filters);
    }

    @GetMapping("/shipments/trends")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public ChartDataResponse shipmentTrends(@ModelAttribute DateRangeRequest filters) {
        return service.shipmentTrendChart(filters);
    }

    @GetMapping("/fleet/vehicle-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public ChartDataResponse vehicleStatus() { return service.vehicleStatusChart(); }

    @GetMapping("/fleet/shipment-volume")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public ChartDataResponse vehicleShipmentVolume(@ModelAttribute DateRangeRequest filters) {
        return service.vehicleShipmentVolume(filters);
    }

    @GetMapping("/fleet/maintenance-cost")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER','FLEET_MANAGER')")
    public ChartDataResponse fleetMaintenanceCost(@ModelAttribute DateRangeRequest filters) {
        return service.maintenanceExpenses(filters);
    }

    @GetMapping("/maintenance/cost-trends")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public ChartDataResponse maintenanceCostTrend(@ModelAttribute DateRangeRequest filters) {
        return service.maintenanceCostTrend(filters);
    }

    @GetMapping("/financial/revenue-trends")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER')")
    public List<ChartDataResponse> revenueTrends(@ModelAttribute DateRangeRequest filters) {
        return service.financialCharts(filters);
    }

    @GetMapping("/financial/payment-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER')")
    public ChartDataResponse paymentStatus(@ModelAttribute DateRangeRequest filters) {
        return service.paymentStatusChart(filters);
    }

    @GetMapping("/financial/payment-method-distribution")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER')")
    public ChartDataResponse paymentMethodDistribution(@ModelAttribute DateRangeRequest filters) {
        return service.paymentMethodDistribution(filters);
    }

    @GetMapping("/financial/customer-revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER')")
    public ChartDataResponse customerRevenue(@ModelAttribute DateRangeRequest filters) {
        return service.customerRevenue(filters);
    }

    @GetMapping("/financial/shipment-revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER')")
    public ChartDataResponse shipmentRevenue(@ModelAttribute DateRangeRequest filters) {
        return service.shipmentRevenue(filters);
    }

    @GetMapping("/financial/maintenance-expenses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER')")
    public ChartDataResponse maintenanceExpenses(@ModelAttribute DateRangeRequest filters) {
        return service.maintenanceExpenses(filters);
    }

    @GetMapping("/delivery/outcomes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public List<ChartDataResponse> deliveryCharts(@ModelAttribute DateRangeRequest filters) {
        return service.deliveryCharts(filters);
    }

    @GetMapping("/financial/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE_MANAGER')")
    public AnalyticsSummaryResponse.Financial financial(@ModelAttribute DateRangeRequest filters) {
        return service.financial(filters);
    }

    @GetMapping("/drivers/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public AnalyticsSummaryResponse.Drivers drivers() { return service.drivers(); }

    @GetMapping("/drivers/performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public Page<DriverPerformanceResponse> driverPerformance(
            @ModelAttribute DateRangeRequest filters,
            @PageableDefault(size=25) Pageable pageable) {
        return service.driverPerformance(filters,pageable);
    }

    @GetMapping("/drivers/performance-chart")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public ChartDataResponse driverPerformanceChart(@ModelAttribute DateRangeRequest filters) {
        return service.driverPerformanceChart(filters);
    }

    @GetMapping("/drivers/availability-distribution")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public ChartDataResponse driverAvailabilityDistribution() {
        return service.driverAvailabilityDistribution();
    }

    @GetMapping("/maintenance/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public AnalyticsSummaryResponse.Maintenance maintenance(@ModelAttribute DateRangeRequest filters) {
        return service.maintenance(filters);
    }

    @GetMapping("/maintenance/type-distribution")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public ChartDataResponse maintenanceTypeDistribution(@ModelAttribute DateRangeRequest filters) {
        return service.maintenanceTypeDistribution(filters);
    }

    @GetMapping("/maintenance/frequency-by-vehicle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public ChartDataResponse maintenanceFrequencyByVehicle(@ModelAttribute DateRangeRequest filters) {
        return service.maintenanceFrequencyByVehicle(filters);
    }

    @GetMapping("/maintenance/upcoming")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER')")
    public Page<MaintenanceScheduleResponse> upcomingMaintenance(
            @ModelAttribute DateRangeRequest filters,
            @PageableDefault(size=25) Pageable pageable) {
        return service.upcomingMaintenance(filters, pageable);
    }

    @GetMapping({"/delivery/summary","/shipments/delivery-performance"})
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public AnalyticsSummaryResponse.Delivery delivery(@ModelAttribute DateRangeRequest filters) {
        return service.delivery(filters);
    }

    @GetMapping("/delivery/on-time-performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public ChartDataResponse deliveryOnTimePerformance(@ModelAttribute DateRangeRequest filters) {
        return service.deliveryOnTimeChart(filters);
    }

    @GetMapping("/delivery/by-route")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public ChartDataResponse deliveryByRoute(@ModelAttribute DateRangeRequest filters) {
        return service.deliveryByRoute(filters);
    }

    @GetMapping("/delivery/by-customer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FLEET_MANAGER','DISPATCHER')")
    public ChartDataResponse deliveryByCustomer(@ModelAttribute DateRangeRequest filters) {
        return service.deliveryByCustomer(filters);
    }
}
