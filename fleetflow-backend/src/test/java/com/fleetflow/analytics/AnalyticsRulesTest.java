package com.fleetflow.analytics;

import com.fleetflow.analytics.dto.DateRangeRequest;
import com.fleetflow.analytics.service.AnalyticsService;
import com.fleetflow.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import com.fleetflow.shipment.enums.ShipmentStatus;
import java.time.Clock;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

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
}
