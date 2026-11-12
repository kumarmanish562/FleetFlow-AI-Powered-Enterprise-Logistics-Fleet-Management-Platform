package com.fleetflow.analytics.repository;

import com.fleetflow.analytics.dto.AnalyticsSummaryResponse;
import com.fleetflow.analytics.dto.ChartDataResponse;
import com.fleetflow.analytics.dto.DriverPerformanceResponse;
import com.fleetflow.analytics.dto.MaintenanceScheduleResponse;
import com.fleetflow.analytics.dto.DateRangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Repository
public class AnalyticsRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public AnalyticsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AnalyticsSummaryResponse.Fleet fleet(LocalDate from, LocalDate to) {
        return fleet(null);
    }

    public AnalyticsSummaryResponse.Fleet fleet(DateRangeRequest filters) {
        UUID vehicleId=filters==null?null:filters.vehicleId();
        var params=new MapSqlParameterSource().addValue("vehicleId",vehicleId);
        var row = one("""
                SELECT count(*) total,
                  count(*) FILTER (WHERE status='AVAILABLE') available,
                  count(*) FILTER (WHERE status IN ('ASSIGNED','IN_TRANSIT')) assigned,
                  count(*) FILTER (WHERE status='MAINTENANCE') maintenance,
                  count(*) FILTER (WHERE status NOT IN ('OUT_OF_SERVICE','RETIRED')) active,
                  count(*) FILTER (WHERE status IN ('OUT_OF_SERVICE','RETIRED')) inactive
                FROM vehicles
                WHERE (cast(:vehicleId as uuid) IS NULL OR id=:vehicleId)
                """, params);
        long total = number(row, "total");
        long assigned = number(row, "assigned");
        BigDecimal coverage = percent(assigned,total);
        return new AnalyticsSummaryResponse.Fleet(total, number(row,"active"), number(row,"available"), assigned,
                number(row,"maintenance"), number(row,"inactive"), coverage);
    }

    public AnalyticsSummaryResponse.Shipments shipments(LocalDate from, LocalDate to) {
        return shipments(new DateRangeRequest(from,to));
    }

    public AnalyticsSummaryResponse.Shipments shipments(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        String where="s.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND s.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')"+shipmentFilters(filters,"s");
        var row = one("""
                SELECT count(*) total,
                  count(*) FILTER (WHERE s.status IN ('CREATED','CONFIRMED')) pending,
                  count(*) FILTER (WHERE s.status IN ('PICKUP_SCHEDULED','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY')) active,
                  count(*) FILTER (WHERE s.status IN ('PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY')) in_transit,
                  count(*) FILTER (WHERE s.status='DELIVERED') delivered,
                  count(*) FILTER (WHERE s.status NOT IN ('DELIVERED','CANCELLED','FAILED','RETURNED')
                    AND s.expected_delivery_date < CURRENT_TIMESTAMP) delayed,
                  count(*) FILTER (WHERE s.status='CANCELLED') cancelled
                FROM shipments s WHERE %s
                """.formatted(where), params);
        return new AnalyticsSummaryResponse.Shipments(number(row,"total"),number(row,"pending"),
                number(row,"active"),number(row,"in_transit"),number(row,"delivered"),
                number(row,"delayed"),number(row,"cancelled"),
                percent(number(row,"delivered"),number(row,"total")));
    }

    public AnalyticsSummaryResponse.Drivers drivers() {
        var row = one("""
                SELECT count(*) total,
                  count(*) FILTER (WHERE employment_status='ACTIVE') active,
                  count(*) FILTER (WHERE employment_status='ACTIVE' AND availability='AVAILABLE') available,
                  count(*) FILTER (WHERE availability IN ('ON_DUTY','ON_DELIVERY')) assigned,
                  count(*) FILTER (WHERE employment_status<>'ACTIVE' OR availability IN ('OFF_DUTY','ON_LEAVE')) unavailable
                FROM drivers
                """, new MapSqlParameterSource());
        return new AnalyticsSummaryResponse.Drivers(number(row,"total"),number(row,"active"),
                number(row,"available"),number(row,"assigned"),number(row,"unavailable"));
    }

    public ChartDataResponse driverAvailabilityDistribution() {
        return chart("driver-availability", "SELECT availability label,count(*) value FROM drivers GROUP BY availability ORDER BY availability",new MapSqlParameterSource());
    }

    public AnalyticsSummaryResponse.Financial financial(LocalDate from, LocalDate to) {
        return financial(new DateRangeRequest(from,to));
    }

    public AnalyticsSummaryResponse.Financial financial(DateRangeRequest filters) {
        LocalDate from=filters.from(), to=filters.to();
        var params = range(from,to).addValue("paymentStatus",filters.paymentStatus()==null?null:filters.paymentStatus().name());
        var invoiced = currencies("""
                SELECT currency, coalesce(sum(total_amount),0) amount FROM billing_invoices
                WHERE status IN ('ISSUED','PARTIALLY_PAID','PAID','OVERDUE') AND created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND created_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                GROUP BY currency ORDER BY currency
                """, params);
        var averageInvoice = currencies("""
                SELECT currency, coalesce(avg(total_amount),0) amount FROM billing_invoices
                WHERE status IN ('ISSUED','PARTIALLY_PAID','PAID','OVERDUE')
                  AND created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND created_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                GROUP BY currency ORDER BY currency
                """,params);
        var collected = currencies("""
                SELECT currency, coalesce(sum(amount),0) amount FROM billing_payments
                WHERE status='SUCCESS'
                  AND (cast(:paymentStatus as varchar) IS NULL OR status=:paymentStatus)
                  AND paid_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND paid_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                GROUP BY currency ORDER BY currency
                """, params);
        var outstanding = currencies("""
                SELECT currency, coalesce(sum(total_amount-amount_paid),0) amount FROM billing_invoices
                WHERE status IN ('ISSUED','PARTIALLY_PAID','OVERDUE')
                  AND created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND created_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                GROUP BY currency ORDER BY currency
                """, params);
        var pending = currencies("""
                SELECT currency, coalesce(sum(amount),0) amount FROM billing_payments
                WHERE status='PENDING'
                  AND (cast(:paymentStatus as varchar) IS NULL OR status=:paymentStatus)
                  AND created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND created_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                GROUP BY currency ORDER BY currency
                """, params);
        Long failed = jdbc.queryForObject("""
                SELECT count(*) FROM billing_payments WHERE status='FAILED'
                  AND (cast(:paymentStatus as varchar) IS NULL OR status=:paymentStatus)
                  AND created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND created_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                """, params, Long.class);
        Long successful=jdbc.queryForObject("""
                SELECT count(*) FROM billing_payments WHERE status='SUCCESS'
                  AND (cast(:paymentStatus as varchar) IS NULL OR status=:paymentStatus)
                  AND paid_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND paid_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                """,params,Long.class);
        return new AnalyticsSummaryResponse.Financial(invoiced,averageInvoice,collected,outstanding,pending,
                successful==null?0:successful,failed == null?0:failed);
    }

    public AnalyticsSummaryResponse.Delivery delivery(LocalDate from, LocalDate to) {
        return delivery(new DateRangeRequest(from,to));
    }

    public AnalyticsSummaryResponse.Delivery delivery(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        String shipmentWhere=shipmentFilters(filters,"s");
        var row = one("""
                SELECT count(*) total,
                  count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL) successful,
                  count(*) FILTER (WHERE d.status='FAILED') failed,
                  count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL AND d.delivered_at <= s.expected_delivery_date) on_time,
                  count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL AND d.delivered_at > s.expected_delivery_date) delayed,
                  count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL) on_time_eligible,
                  avg(extract(epoch from (d.delivered_at - s.created_at))/3600.0)
                    FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL) avg_hours,
                  avg(r.distance_km) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL) average_estimated_route_distance_km
                FROM deliveries d JOIN shipments s ON s.id=d.shipment_id
                LEFT JOIN routes r ON r.id=s.route_id
                WHERE coalesce(d.delivered_at,d.updated_at,d.created_at) >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND coalesce(d.delivered_at,d.updated_at,d.created_at) < coalesce(cast(:toAt as timestamptz), 'infinity')
                """+shipmentWhere, params);
        long total=number(row,"total"), success=number(row,"successful"), onTime=number(row,"on_time");
        long failed=number(row,"failed");
        return new AnalyticsSummaryResponse.Delivery(total,success,failed,onTime,
                number(row,"delayed"),percent(success,success+failed),percent(onTime,number(row,"on_time_eligible")),
                decimal(row,"avg_hours"),decimal(row,"average_estimated_route_distance_km"));
    }

    public AnalyticsSummaryResponse.Maintenance maintenance(LocalDate from, LocalDate to, String currency) {
        return maintenance(new DateRangeRequest(from,to),currency);
    }

    public AnalyticsSummaryResponse.Maintenance maintenance(DateRangeRequest filters, String currency) {
        var params=new MapSqlParameterSource().addValue("fromDate",filters.from()).addValue("toDate",filters.to())
                .addValue("maintenanceType",filters.maintenanceType()==null?null:filters.maintenanceType().name())
                .addValue("vehicleId",filters.vehicleId());
        var row = one("""
                SELECT count(*) total,
                  count(*) FILTER (WHERE m.status='SCHEDULED') scheduled,
                  count(*) FILTER (WHERE m.status='IN_PROGRESS') in_progress,
                  count(*) FILTER (WHERE m.status='COMPLETED') completed,
                  coalesce(sum(m.cost),0) cost,
                  (SELECT count(*) FROM vehicles WHERE status='MAINTENANCE') vehicles_under_maintenance
                FROM maintenance_records m
                WHERE m.maintenance_date >= coalesce(cast(:fromDate as date), '-infinity')
                  AND m.maintenance_date <= coalesce(cast(:toDate as date), 'infinity')
                  AND (cast(:maintenanceType as varchar) IS NULL OR m.maintenance_type=:maintenanceType)
                  AND (cast(:vehicleId as uuid) IS NULL OR m.vehicle_id=:vehicleId)
                """, params);
        return new AnalyticsSummaryResponse.Maintenance(number(row,"total"),number(row,"scheduled"),
                number(row,"in_progress"),number(row,"completed"),number(row,"vehicles_under_maintenance"),
                List.of(new AnalyticsSummaryResponse.CurrencyAmount(currency,decimal(row,"cost"))));
    }

    public List<ChartDataResponse> shipmentCharts(DateRangeRequest filters) {
        var params=range(filters.from(),filters.to());
        String extra=shipmentFilters(filters,"s");
        params=withFilterParams(params,filters);
        String base="s.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND s.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')";
        return List.of(
                chart("shipment-created-"+filters.bucket().name().toLowerCase(), "SELECT to_char(date_trunc('"+filters.bucket().postgresUnit()+"',s.created_at AT TIME ZONE 'UTC'),'"+filters.bucket().labelFormat()+"') label,count(*) value FROM shipments s WHERE "+base+extra+" GROUP BY 1 ORDER BY 1",params),
                chart("shipment-status", "SELECT s.status label,count(*) value FROM shipments s WHERE "+base+extra+" GROUP BY s.status ORDER BY s.status",params),
                chart("shipment-priority", "SELECT s.priority label,count(*) value FROM shipments s WHERE "+base+extra+" GROUP BY s.priority ORDER BY s.priority",params),
                chart("shipment-customer", "SELECT c.name label,count(*) value FROM shipments s JOIN customers c ON c.id=s.customer_id WHERE "+base+extra+" GROUP BY c.id,c.name ORDER BY c.name",params),
                chart("shipment-origin-destination", "SELECT s.origin_address || ' → ' || s.destination_address label,count(*) value FROM shipments s WHERE "+base+extra+" GROUP BY s.origin_address,s.destination_address ORDER BY value DESC,label",params));
    }

    public ChartDataResponse shipmentStatusChart(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        String where="s.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND s.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')"+shipmentFilters(filters,"s");
        return chart("shipment-status", "SELECT s.status label,count(*) value FROM shipments s WHERE "+where+" GROUP BY s.status ORDER BY s.status",params);
    }

    public ChartDataResponse shipmentTrendChart(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        var bucket=filters.bucket();
        String where="s.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND s.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')"+shipmentFilters(filters,"s");
        String sql="SELECT to_char(date_trunc('%s',s.created_at AT TIME ZONE 'UTC'),'%s') label,count(*) value FROM shipments s WHERE %s GROUP BY 1 ORDER BY 1".formatted(bucket.postgresUnit(),bucket.labelFormat(),where);
        return chart("shipment-created-"+bucket.name().toLowerCase(),sql,params);
    }

    public ChartDataResponse vehicleStatusChart() {
        return chart("vehicle-status", "SELECT status label,count(*) value FROM vehicles GROUP BY status ORDER BY status",new MapSqlParameterSource());
    }

    public List<ChartDataResponse> fleetCharts(LocalDate from, LocalDate to) {
        return List.of(
                vehicleStatusChart(),
                chart("vehicle-type", "SELECT vt.name label,count(*) value FROM vehicles v JOIN vehicle_types vt ON vt.id=v.vehicle_type_id GROUP BY vt.id,vt.name ORDER BY vt.name",new MapSqlParameterSource()),
                maintenanceCostChart(from,to));
    }

    public ChartDataResponse maintenanceCostChart(LocalDate from, LocalDate to) {
        return chart("maintenance-cost-month", "SELECT to_char(date_trunc('month',maintenance_date),'YYYY-MM') label,coalesce(sum(cost),0) value FROM maintenance_records WHERE maintenance_date >= coalesce(cast(:fromDate as date), '-infinity') AND maintenance_date <= coalesce(cast(:toDate as date), 'infinity') GROUP BY 1 ORDER BY 1",new MapSqlParameterSource().addValue("fromDate",from).addValue("toDate",to));
    }

    public ChartDataResponse maintenanceCostChart(DateRangeRequest filters) {
        var params=new MapSqlParameterSource().addValue("fromDate",filters.from()).addValue("toDate",filters.to())
                .addValue("maintenanceType",filters.maintenanceType()==null?null:filters.maintenanceType().name())
                .addValue("vehicleId",filters.vehicleId());
        String sql="""
                SELECT to_char(date_trunc('month',m.maintenance_date),'YYYY-MM') label,
                       coalesce(sum(m.cost),0) value
                FROM maintenance_records m
                WHERE m.maintenance_date >= coalesce(cast(:fromDate as date), '-infinity')
                  AND m.maintenance_date <= coalesce(cast(:toDate as date), 'infinity')
                  AND (cast(:maintenanceType as varchar) IS NULL OR m.maintenance_type=:maintenanceType)
                  AND (cast(:vehicleId as uuid) IS NULL OR m.vehicle_id=:vehicleId)
                GROUP BY 1 ORDER BY 1
                """;
        return chart("maintenance-cost-month",sql,params);
    }

    public ChartDataResponse maintenanceCostByVehicle(LocalDate from, LocalDate to) {
        return chart("maintenance-cost-vehicle", "SELECT v.registration_number label,coalesce(sum(m.cost),0) value FROM maintenance_records m JOIN vehicles v ON v.id=m.vehicle_id WHERE m.maintenance_date >= coalesce(cast(:fromDate as date), '-infinity') AND m.maintenance_date <= coalesce(cast(:toDate as date), 'infinity') GROUP BY v.id,v.registration_number ORDER BY v.registration_number",new MapSqlParameterSource().addValue("fromDate",from).addValue("toDate",to));
    }

    public ChartDataResponse maintenanceTypeDistribution(DateRangeRequest filters) {
        var params=maintenanceParams(filters);
        String sql="SELECT maintenance_type label,count(*) value FROM maintenance_records WHERE maintenance_date >= coalesce(cast(:fromDate as date), '-infinity') AND maintenance_date <= coalesce(cast(:toDate as date), 'infinity') AND (cast(:maintenanceType as varchar) IS NULL OR maintenance_type=:maintenanceType) AND (cast(:vehicleId as uuid) IS NULL OR vehicle_id=:vehicleId) GROUP BY maintenance_type ORDER BY maintenance_type";
        return chart("maintenance-type-distribution",sql,params);
    }

    public ChartDataResponse maintenanceFrequencyByVehicle(DateRangeRequest filters) {
        var params=maintenanceParams(filters);
        String sql="SELECT v.registration_number label,count(*) value FROM maintenance_records m JOIN vehicles v ON v.id=m.vehicle_id WHERE m.maintenance_date >= coalesce(cast(:fromDate as date), '-infinity') AND m.maintenance_date <= coalesce(cast(:toDate as date), 'infinity') AND (cast(:maintenanceType as varchar) IS NULL OR m.maintenance_type=:maintenanceType) AND (cast(:vehicleId as uuid) IS NULL OR m.vehicle_id=:vehicleId) GROUP BY v.id,v.registration_number ORDER BY value DESC,v.registration_number";
        return chart("maintenance-frequency-by-vehicle",sql,params);
    }

    public ChartDataResponse deliveryOnTimeChart(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        String sql="""
                SELECT outcome label,count(*) value FROM (
                    SELECT CASE
                        WHEN s.expected_delivery_date IS NULL THEN 'PROMISE_DATE_MISSING'
                        WHEN d.delivered_at <= s.expected_delivery_date THEN 'ON_TIME'
                        ELSE 'LATE'
                    END outcome
                    FROM deliveries d JOIN shipments s ON s.id=d.shipment_id
                    WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL
                      AND coalesce(d.delivered_at,d.updated_at,d.created_at) >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                      AND coalesce(d.delivered_at,d.updated_at,d.created_at) < coalesce(cast(:toAt as timestamptz), 'infinity')
                """+shipmentFilters(filters,"s")+"""
                ) completed GROUP BY outcome ORDER BY outcome
                """;
        return chart("delivery-on-time-outcomes",sql,params);
    }

    public ChartDataResponse deliveryByRoute(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        String where="coalesce(d.delivered_at,d.updated_at,d.created_at) >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND coalesce(d.delivered_at,d.updated_at,d.created_at) < coalesce(cast(:toAt as timestamptz), 'infinity')"+shipmentFilters(filters,"s");
        String sql="""
                SELECT r.route_number label,
                       coalesce(round(100.0 * count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL AND s.expected_delivery_date IS NOT NULL AND d.delivered_at<=s.expected_delivery_date)
                         / nullif(count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL AND s.expected_delivery_date IS NOT NULL),0),2),0)::numeric value
                FROM deliveries d JOIN shipments s ON s.id=d.shipment_id JOIN routes r ON r.id=s.route_id
                WHERE %s
                GROUP BY r.id,r.route_number ORDER BY r.route_number
                """.formatted(where);
        return chart("delivery-on-time-by-route-percent",sql,params);
    }

    public List<RouteDelayMetric> routeDelaysForAI(Instant fromInclusive, Instant toExclusive, UUID organizationId) {
        return jdbc.query("""
                SELECT r.route_number AS route_number,
                       count(d.id) AS completed_deliveries,
                       count(d.id) FILTER (WHERE s.expected_delivery_date IS NOT NULL
                           AND d.delivered_at > s.expected_delivery_date) AS delayed_deliveries
                FROM deliveries d
                JOIN shipments s ON s.id=d.shipment_id AND s.organization_id=d.organization_id
                JOIN routes r ON r.id=s.route_id AND r.organization_id=s.organization_id
                WHERE d.organization_id=:organizationId
                  AND d.status='DELIVERED'
                  AND d.delivered_at >= :fromInclusive
                  AND d.delivered_at < :toExclusive
                GROUP BY r.id,r.route_number
                ORDER BY delayed_deliveries DESC,completed_deliveries DESC,r.route_number
                LIMIT 20
                """, new MapSqlParameterSource().addValue("organizationId", organizationId)
                .addValue("fromInclusive", fromInclusive).addValue("toExclusive", toExclusive),
                (rs, row) -> new RouteDelayMetric(rs.getString("route_number"),
                        rs.getLong("completed_deliveries"), rs.getLong("delayed_deliveries")));
    }

    public record RouteDelayMetric(String routeNumber, long completedDeliveries, long delayedDeliveries) {}

    public AnalyticsSummaryResponse.Fleet fleetForAI(UUID organizationId) {
        var row = one("""
                SELECT count(*) total,
                  count(*) FILTER (WHERE status NOT IN ('OUT_OF_SERVICE','RETIRED')) active,
                  count(*) FILTER (WHERE status='AVAILABLE') available,
                  count(*) FILTER (WHERE status IN ('ASSIGNED','IN_TRANSIT')) assigned,
                  count(*) FILTER (WHERE status='MAINTENANCE') maintenance,
                  count(*) FILTER (WHERE status IN ('OUT_OF_SERVICE','RETIRED')) inactive
                FROM vehicles WHERE organization_id=:organizationId
                """, new MapSqlParameterSource("organizationId", organizationId));
        long total = number(row, "total");
        long assigned = number(row, "assigned");
        return new AnalyticsSummaryResponse.Fleet(total, number(row, "active"), number(row, "available"),
                assigned, number(row, "maintenance"), number(row, "inactive"), percent(assigned, total));
    }

    public AnalyticsSummaryResponse.Delivery deliveryPerformanceForAI(
            Instant fromInclusive, Instant toExclusive, UUID organizationId) {
        var row = one("""
                SELECT count(d.id) total,
                  count(d.id) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL) successful,
                  count(d.id) FILTER (WHERE d.status='FAILED') failed,
                  count(d.id) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL AND d.delivered_at <= s.expected_delivery_date) on_time,
                  count(d.id) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL AND d.delivered_at > s.expected_delivery_date) delayed,
                  count(d.id) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL) on_time_eligible,
                  avg(extract(epoch from (d.delivered_at-s.created_at))/3600.0)
                    FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL) avg_hours,
                  avg(r.distance_km) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL) average_distance
                FROM deliveries d
                JOIN shipments s ON s.id=d.shipment_id AND s.organization_id=d.organization_id
                LEFT JOIN routes r ON r.id=s.route_id AND r.organization_id=s.organization_id
                WHERE d.organization_id=:organizationId
                  AND coalesce(d.delivered_at,d.updated_at,d.created_at) >= :fromInclusive
                  AND coalesce(d.delivered_at,d.updated_at,d.created_at) < :toExclusive
                """, new MapSqlParameterSource("organizationId", organizationId)
                .addValue("fromInclusive", fromInclusive).addValue("toExclusive", toExclusive));
        long successful = number(row, "successful");
        long failed = number(row, "failed");
        return new AnalyticsSummaryResponse.Delivery(number(row, "total"), successful, failed,
                number(row, "on_time"), number(row, "delayed"), percent(successful, successful + failed),
                percent(number(row, "on_time"), number(row, "on_time_eligible")), decimal(row, "avg_hours"),
                decimal(row, "average_distance"));
    }

    public ChartDataResponse deliveryByCustomer(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        String where="coalesce(d.delivered_at,d.updated_at,d.created_at) >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND coalesce(d.delivered_at,d.updated_at,d.created_at) < coalesce(cast(:toAt as timestamptz), 'infinity')"+shipmentFilters(filters,"s");
        String sql="""
                SELECT c.name label,
                       coalesce(round(100.0 * count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL AND s.expected_delivery_date IS NOT NULL AND d.delivered_at<=s.expected_delivery_date)
                         / nullif(count(*) FILTER (WHERE d.status='DELIVERED' AND d.delivered_at IS NOT NULL AND s.expected_delivery_date IS NOT NULL),0),2),0)::numeric value
                FROM deliveries d JOIN shipments s ON s.id=d.shipment_id JOIN customers c ON c.id=s.customer_id
                WHERE %s
                GROUP BY c.id,c.name ORDER BY c.name
                """.formatted(where);
        return chart("delivery-on-time-by-customer-percent",sql,params);
    }

    private MapSqlParameterSource maintenanceParams(DateRangeRequest filters) {
        return new MapSqlParameterSource().addValue("fromDate",filters.from()).addValue("toDate",filters.to())
                .addValue("maintenanceType",filters.maintenanceType()==null?null:filters.maintenanceType().name())
                .addValue("vehicleId",filters.vehicleId());
    }

    public ChartDataResponse vehicleShipmentVolume(LocalDate from, LocalDate to) {
        return vehicleShipmentVolume(new DateRangeRequest(from,to));
    }

    public ChartDataResponse vehicleShipmentVolume(DateRangeRequest filters) {
        var params=range(filters.from(),filters.to()).addValue("vehicleId",filters.vehicleId());
        String sql="SELECT v.registration_number label,count(s.id) value FROM vehicles v LEFT JOIN shipments s ON s.vehicle_id=v.id AND s.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND s.created_at < coalesce(cast(:toAt as timestamptz), 'infinity') WHERE (cast(:vehicleId as uuid) IS NULL OR v.id=:vehicleId) GROUP BY v.id,v.registration_number ORDER BY v.registration_number";
        return chart("vehicle-shipment-volume",sql,params);
    }

    public ChartDataResponse driverDeliveryPerformance(LocalDate from, LocalDate to) {
        return driverDeliveryPerformance(new DateRangeRequest(from,to));
    }

    public ChartDataResponse driverDeliveryPerformance(DateRangeRequest filters) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters);
        String shipmentWhere="s.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND s.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')"+shipmentFilters(filters,"s");
        String sql="SELECT d.first_name || ' ' || d.last_name label,count(*) FILTER (WHERE de.status='DELIVERED' AND de.delivered_at IS NOT NULL) value FROM drivers d LEFT JOIN shipments s ON s.driver_id=d.id AND "+shipmentWhere+" LEFT JOIN deliveries de ON de.shipment_id=s.id GROUP BY d.id,d.first_name,d.last_name ORDER BY value DESC,label LIMIT 20";
        return chart("driver-deliveries",sql,params);
    }

    public ChartDataResponse paymentStatusChart(LocalDate from, LocalDate to) {
        return paymentStatusChart(new DateRangeRequest(from,to));
    }

    public ChartDataResponse paymentStatusChart(DateRangeRequest filters) {
        var params=range(filters.from(),filters.to()).addValue("paymentStatus",filters.paymentStatus()==null?null:filters.paymentStatus().name());
        String statusFilter=filters.paymentStatus()==null?"":" AND p.status=:paymentStatus";
        return chart("payment-status", "SELECT p.currency || ' ' || p.status label,count(*) value FROM billing_payments p WHERE p.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND p.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')"+statusFilter+" GROUP BY p.currency,p.status ORDER BY p.currency,p.status",params);
    }

    public List<ChartDataResponse> revenueTrends(DateRangeRequest filters) {
        LocalDate from=filters.from(), to=filters.to();
        var bucket=filters.bucket();
        var params=range(from,to).addValue("paymentStatus",filters.paymentStatus()==null?null:filters.paymentStatus().name());
        String periodSql="""
                SELECT currency, to_char(date_trunc('%s',paid_at AT TIME ZONE 'UTC'),'%s') label,
                       coalesce(sum(amount),0) value
                FROM billing_payments
                WHERE status='SUCCESS'
                  AND (cast(:paymentStatus as varchar) IS NULL OR status=:paymentStatus)
                  AND paid_at >= coalesce(cast(:fromAt as timestamptz), '-infinity')
                  AND paid_at < coalesce(cast(:toAt as timestamptz), 'infinity')
                GROUP BY currency, date_trunc('%s',paid_at AT TIME ZONE 'UTC')
                ORDER BY currency, date_trunc('%s',paid_at AT TIME ZONE 'UTC')
                """.formatted(bucket.postgresUnit(),bucket.labelFormat(),bucket.postgresUnit(),bucket.postgresUnit());
        var rows=jdbc.query(periodSql,params,(rs,n)->new Object[]{rs.getString("currency"),rs.getString("label"),rs.getBigDecimal("value")});
        return rows.stream().collect(java.util.stream.Collectors.groupingBy(
                row->(String)row[0],java.util.LinkedHashMap::new,java.util.stream.Collectors.toList()))
                .entrySet().stream().map(entry->new ChartDataResponse("revenue-"+bucket.name().toLowerCase()+"-"+entry.getKey(),
                        entry.getValue().stream().map(row->new ChartDataResponse.Point((String)row[1],(BigDecimal)row[2])).toList())).toList();
    }

    public ChartDataResponse customerRevenue(DateRangeRequest filters) {
        var params=range(filters.from(),filters.to()).addValue("paymentStatus",filters.paymentStatus()==null?null:filters.paymentStatus().name());
        String sql="SELECT 'billing-account-' || p.customer_id || ' / ' || p.currency label, sum(p.amount) value FROM billing_payments p WHERE p.status='SUCCESS' AND p.paid_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND p.paid_at < coalesce(cast(:toAt as timestamptz), 'infinity') AND (cast(:paymentStatus as varchar) IS NULL OR p.status=:paymentStatus) GROUP BY p.customer_id,p.currency ORDER BY p.customer_id,p.currency";
        return chart("revenue-by-billing-customer",sql,params);
    }

    public ChartDataResponse paymentMethodDistribution(DateRangeRequest filters) {
        var params=range(filters.from(),filters.to()).addValue("paymentStatus",filters.paymentStatus()==null?null:filters.paymentStatus().name());
        String statusFilter=filters.paymentStatus()==null?"":" AND p.status=:paymentStatus";
        return chart("payment-method-distribution", "SELECT p.currency || ' ' || p.method label,count(*) value FROM billing_payments p WHERE p.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND p.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')"+statusFilter+" GROUP BY p.currency,p.method ORDER BY p.currency,p.method",params);
    }

    public ChartDataResponse revenueByBillingShipment(DateRangeRequest filters) {
        var params=range(filters.from(),filters.to()).addValue("paymentStatus",filters.paymentStatus()==null?null:filters.paymentStatus().name());
        String sql="SELECT 'billing-shipment-' || i.shipment_id || ' / ' || p.currency label,sum(p.amount) value FROM billing_payments p JOIN billing_invoices i ON i.id=p.invoice_id WHERE p.status='SUCCESS' AND p.paid_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND p.paid_at < coalesce(cast(:toAt as timestamptz), 'infinity') AND (cast(:paymentStatus as varchar) IS NULL OR p.status=:paymentStatus) GROUP BY i.shipment_id,p.currency ORDER BY i.shipment_id,p.currency";
        return chart("revenue-by-billing-shipment",sql,params);
    }

    public Page<DriverPerformanceResponse> driverPerformance(LocalDate from, LocalDate to, Pageable pageable) {
        return driverPerformance(new DateRangeRequest(from,to),pageable);
    }

    public Page<DriverPerformanceResponse> driverPerformance(DateRangeRequest filters, Pageable pageable) {
        var params=withFilterParams(range(filters.from(),filters.to()),filters)
                .addValue("limit",pageable.getPageSize()).addValue("offset",pageable.getOffset());
        String where="s.created_at >= coalesce(cast(:fromAt as timestamptz), '-infinity') AND s.created_at < coalesce(cast(:toAt as timestamptz), 'infinity')"+shipmentFilters(filters,"s");
        String driverFilter=filters.driverId()==null?"":"WHERE d.id=:driverId";
        String query="""
                SELECT d.id,d.first_name,d.last_name,
                  count(s.id) assigned,
                  count(*) FILTER (WHERE de.status='DELIVERED' AND de.delivered_at IS NOT NULL) successful,
                  count(*) FILTER (WHERE de.status='FAILED') failed,
                  count(*) FILTER (WHERE de.status='DELIVERED' AND de.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL AND de.delivered_at <= s.expected_delivery_date) on_time,
                  count(*) FILTER (WHERE de.status='DELIVERED' AND de.delivered_at IS NOT NULL
                    AND s.expected_delivery_date IS NOT NULL) on_time_eligible,
                  avg(extract(epoch from (de.delivered_at-s.created_at))/3600.0)
                    FILTER (WHERE de.status='DELIVERED' AND de.delivered_at IS NOT NULL) avg_hours
                FROM drivers d LEFT JOIN shipments s ON s.driver_id=d.id AND """+where+"""
                LEFT JOIN deliveries de ON de.shipment_id=s.id
                %s
                GROUP BY d.id,d.first_name,d.last_name ORDER BY d.last_name,d.first_name,d.id
                LIMIT :limit OFFSET :offset
                """.formatted(driverFilter);
        var rows=jdbc.query(query,params,(rs,n)->new DriverPerformanceResponse(rs.getObject("id",java.util.UUID.class),
                rs.getString("first_name"),rs.getString("last_name"),rs.getLong("assigned"),
                rs.getLong("successful"),rs.getLong("failed"),rs.getLong("on_time"),
                percent(rs.getLong("on_time"),rs.getLong("on_time_eligible")),rs.getBigDecimal("avg_hours")));
        Long total=jdbc.queryForObject("SELECT count(*) FROM drivers d "+driverFilter,params,Long.class);
        return new PageImpl<>(rows,pageable,total==null?0:total);
    }

    public Page<MaintenanceScheduleResponse> upcomingMaintenance(LocalDate today, DateRangeRequest filters, Pageable pageable) {
        var params=new MapSqlParameterSource().addValue("today",today)
                .addValue("through",today.plusDays(30)).addValue("limit",pageable.getPageSize())
                .addValue("offset",pageable.getOffset())
                .addValue("fromDate",filters.from()).addValue("toDate",filters.to())
                .addValue("maintenanceType",filters.maintenanceType()==null?null:filters.maintenanceType().name())
                .addValue("vehicleId",filters.vehicleId());
        String typeFilter=(filters.maintenanceType()==null?"":" AND m.maintenance_type=:maintenanceType")
                +(filters.vehicleId()==null?"":" AND m.vehicle_id=:vehicleId");
        String dateFilter=filters.from()!=null||filters.to()!=null
                ?" AND m.next_service_date >= coalesce(cast(:fromDate as date), '-infinity') AND m.next_service_date <= coalesce(cast(:toDate as date), 'infinity')"
                :" AND (m.next_service_date < :today OR m.next_service_date BETWEEN :today AND :through)";
        var rows=jdbc.query("""
                SELECT m.id maintenance_id,v.id vehicle_id,v.registration_number,
                       m.maintenance_type,m.status,m.next_service_date,
                       (m.next_service_date < :today) overdue
                FROM maintenance_records m JOIN vehicles v ON v.id=m.vehicle_id
                WHERE m.status='SCHEDULED'
                """+dateFilter+typeFilter+"""
                ORDER BY m.next_service_date,m.id LIMIT :limit OFFSET :offset
                """,params,(rs,n)->new MaintenanceScheduleResponse(
                rs.getObject("maintenance_id",java.util.UUID.class),
                rs.getObject("vehicle_id",java.util.UUID.class),rs.getString("registration_number"),
                rs.getString("maintenance_type"),rs.getString("status"),
                rs.getObject("next_service_date",LocalDate.class),rs.getBoolean("overdue")));
        Long total=jdbc.queryForObject("""
                SELECT count(*) FROM maintenance_records m
                WHERE m.status='SCHEDULED'
                """+dateFilter.replace("m.","")+(filters.maintenanceType()==null?"":" AND maintenance_type=:maintenanceType")
                +(filters.vehicleId()==null?"":" AND vehicle_id=:vehicleId")+"""
                """,params,Long.class);
        return new PageImpl<>(rows,pageable,total==null?0:total);
    }

    private String shipmentFilters(DateRangeRequest f,String alias) {
        String p=alias.isBlank()?"":alias+".";
        StringBuilder where=new StringBuilder();
        if(f.status()!=null) where.append(" AND ").append(p).append("status=:shipmentStatus");
        if(f.vehicleId()!=null) where.append(" AND ").append(p).append("vehicle_id=:vehicleId");
        if(f.driverId()!=null) where.append(" AND ").append(p).append("driver_id=:driverId");
        if(f.customerId()!=null) where.append(" AND ").append(p).append("customer_id=:customerId");
        if(f.routeId()!=null) where.append(" AND ").append(p).append("route_id=:routeId");
        return where.toString();
    }

    private MapSqlParameterSource withFilterParams(MapSqlParameterSource p,DateRangeRequest f) {
        return p.addValue("shipmentStatus",f.status()==null?null:f.status().name())
                .addValue("vehicleId",f.vehicleId()).addValue("driverId",f.driverId())
                .addValue("customerId",f.customerId()).addValue("routeId",f.routeId());
    }

    private ChartDataResponse chart(String key,String sql,MapSqlParameterSource params) {
        return new ChartDataResponse(key,jdbc.query(sql,params,(rs,n)->new ChartDataResponse.Point(rs.getString("label"),rs.getBigDecimal("value"))));
    }
    private List<AnalyticsSummaryResponse.CurrencyAmount> currencies(String sql,MapSqlParameterSource params) {
        return jdbc.query(sql,params,(rs,n)->new AnalyticsSummaryResponse.CurrencyAmount(rs.getString("currency"),rs.getBigDecimal("amount")));
    }
    private MapSqlParameterSource range(LocalDate from,LocalDate to) {
        OffsetDateTime start=from==null?null:from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end=to==null?null:to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return new MapSqlParameterSource().addValue("fromAt",start).addValue("toAt",end);
    }
    private java.util.Map<String,Object> one(String sql,MapSqlParameterSource params) {
        return jdbc.queryForMap(sql,params);
    }
    private long number(java.util.Map<String,Object> row,String key) { return ((Number)row.get(key)).longValue(); }
    private BigDecimal decimal(java.util.Map<String,Object> row,String key) { return (BigDecimal)row.get(key); }
    private BigDecimal percent(long part,long total) {
        return total==0?BigDecimal.ZERO:BigDecimal.valueOf(part).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total),2,java.math.RoundingMode.HALF_UP);
    }
}
