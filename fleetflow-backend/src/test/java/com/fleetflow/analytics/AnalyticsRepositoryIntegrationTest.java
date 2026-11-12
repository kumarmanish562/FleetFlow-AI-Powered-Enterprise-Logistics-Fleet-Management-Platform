package com.fleetflow.analytics;

import com.fleetflow.analytics.dto.DateRangeRequest;
import com.fleetflow.analytics.enums.AnalyticsTimeBucket;
import com.fleetflow.analytics.repository.AnalyticsRepository;
import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.shipment.enums.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AnalyticsRepository.class)
class AnalyticsRepositoryIntegrationTest extends PostgresContainerTest {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Test
    void aggregateQueriesRunAgainstPostgresForEmptyDateWindowAndFilters() {
        LocalDate today = LocalDate.now();
        DateRangeRequest filters = new DateRangeRequest(
                today.minusDays(7), today, ShipmentStatus.IN_TRANSIT,
                null, null, null, null, null, null, AnalyticsTimeBucket.WEEKLY);

        assertThat(analyticsRepository.shipments(filters).total()).isZero();
        assertThat(analyticsRepository.shipmentStatusChart(filters).points()).isEmpty();
        assertThat(analyticsRepository.shipmentTrendChart(filters).points()).isEmpty();
        assertThat(analyticsRepository.shipmentCharts(filters)).hasSize(5);
        assertThat(analyticsRepository.revenueTrends(filters)).isEmpty();
        assertThat(analyticsRepository.customerRevenue(filters).points()).isEmpty();
        assertThat(analyticsRepository.paymentMethodDistribution(filters).points()).isEmpty();
        assertThat(analyticsRepository.revenueByBillingShipment(filters).points()).isEmpty();
        assertThat(analyticsRepository.financial(today.minusDays(7), today).failedPaymentCount()).isZero();
        assertThat(analyticsRepository.delivery(today.minusDays(7), today).total()).isZero();
        assertThat(analyticsRepository.maintenance(today.minusDays(7), today, "INR").recordCount()).isZero();
        assertThat(analyticsRepository.driverPerformance(today.minusDays(7), today, PageRequest.of(0, 10)).getTotalElements()).isZero();
        assertThat(analyticsRepository.upcomingMaintenance(today, filters, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void operationalAiAnalyticsQueriesAreTenantScopedAndHandleEmptyData() {
        UUID organizationId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        LocalDate today = LocalDate.now();

        assertThat(analyticsRepository.fleetForAI(organizationId).total()).isZero();
        assertThat(analyticsRepository.deliveryPerformanceForAI(
                today.minusDays(7).atStartOfDay().toInstant(ZoneOffset.UTC),
                today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC), organizationId).total()).isZero();
        assertThat(analyticsRepository.routeDelaysForAI(
                today.minusDays(7).atStartOfDay().toInstant(ZoneOffset.UTC),
                today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC), organizationId)).isEmpty();
    }
}
