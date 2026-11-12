package com.fleetflow.analytics;

import com.fleetflow.analytics.dto.DateRangeRequest;
import com.fleetflow.analytics.service.AnalyticsService;
import com.fleetflow.analytics.repository.AnalyticsRepository;
import com.fleetflow.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import com.fleetflow.shipment.enums.ShipmentStatus;
import java.time.Clock;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsRulesTest {
    @Test
    void rejectsInvertedDateRange() {
        assertThrows(BadRequestException.class, () ->
                new DateRangeRequest(LocalDate.parse("2026-02-02"), LocalDate.parse("2026-02-01")));
    }

    @Test
    void permitsOpenEndedRange() {
        assertDoesNotThrow(() -> new DateRangeRequest(LocalDate.parse("2026-02-01"), null));
    }

    @Test
    void percentageUsesTwoDecimalPlacesAndHandlesEmptyData() {
        assertEquals(new BigDecimal("33.33"), AnalyticsService.percentage(1,3));
        assertEquals(BigDecimal.ZERO, AnalyticsService.percentage(0,0));
    }

    @Test
    void rejectsFiltersThatCannotBeAppliedToFinancialReports() {
        var service = new AnalyticsService(null, "INR", Clock.systemUTC());
        var filters = new DateRangeRequest(null, null, ShipmentStatus.DELIVERED,
                null, null, UUID.randomUUID(), null, null, null, null);

        assertThrows(BadRequestException.class, () -> service.financial(filters));
    }

    @Test
    void defaultsChartBucketToDaily() {
        assertEquals(com.fleetflow.analytics.enums.AnalyticsTimeBucket.DAILY,
                new DateRangeRequest(null, null).bucket());
    }

    @Test
    void aiRouteAnalyticsUsesAuthenticatedOrganizationAndUtcInclusiveDates() {
        AnalyticsRepository repository = mock(AnalyticsRepository.class);
        var service = new AnalyticsService(repository, "INR", Clock.systemUTC());
        UUID organizationId = UUID.randomUUID();
        LocalDate from = LocalDate.parse("2026-10-01");
        LocalDate to = LocalDate.parse("2026-10-03");
        when(repository.routeDelaysForAI(any(Instant.class), any(Instant.class), eq(organizationId)))
                .thenReturn(List.of());

        service.routeDelaysForAI(from, to, organizationId);

        verify(repository).routeDelaysForAI(from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC), organizationId);
    }

    @Test
    void aiAnalyticsRejectsRangesLongerThanNinetyDays() {
        var service = new AnalyticsService(mock(AnalyticsRepository.class), "INR", Clock.systemUTC());

        assertThrows(BadRequestException.class, () -> service.routeDelaysForAI(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-04-01"), UUID.randomUUID()));
        assertThrows(BadRequestException.class, () -> service.deliveryPerformanceForAI(
                LocalDate.parse("2026-02-02"), LocalDate.parse("2026-02-01"), UUID.randomUUID()));
    }
}
