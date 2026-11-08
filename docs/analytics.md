# Analytics API

Phase 14 provides database-backed, read-only analytics under `/api/v1/analytics`.
All `from` and `to` parameters use ISO dates (`YYYY-MM-DD`), interpreted as
UTC boundaries; `to` is inclusive. An inverted range returns the standard
`400 BAD_REQUEST` response.

## Endpoints

- `GET /dashboard` and `/dashboard/summary`
- `GET /fleet/summary`, `/fleet/vehicle-status`, `/fleet/utilization`, `/fleet/maintenance-cost`
- `GET /fleet/shipment-volume`
- `GET /shipments/summary`, `/shipments/status-distribution`, `/shipments/trends`, `/shipments/delivery-performance`
- `GET /financial/summary`, `/financial/revenue-trends`, `/financial/payment-status`, `/financial/customer-revenue`, `/financial/shipment-revenue`, `/financial/payment-method-distribution`, `/financial/maintenance-expenses`
- `GET /drivers/summary`, `/drivers/performance`, `/drivers/performance-chart`, `/drivers/availability-distribution`
- `GET /maintenance/summary`, `/maintenance/cost-trends`, `/maintenance/type-distribution`, `/maintenance/frequency-by-vehicle`, `/maintenance/upcoming`
- `GET /delivery/summary`, `/delivery/outcomes`, `/delivery/on-time-performance`, `/delivery/by-route`, `/delivery/by-customer`

Paged driver and maintenance reports use Spring Data `page` and `size` parameters.
Upcoming maintenance defaults to overdue records plus the next 30 days; supplying
`from` or `to` switches to an inclusive next-service-date window.
Each chart URL returns only its named dataset. Shipment chart ranges accept
`bucket=DAILY|WEEKLY|MONTHLY|YEARLY`; revenue trends accept the same bucket and
return one series per currency. Filters that do not apply to an endpoint return
the standard `400 BAD_REQUEST` instead of being silently ignored.

Filter scope is endpoint-specific: shipment and delivery reports accept shipment
status and vehicle, driver, customer, and route IDs; maintenance reports accept
vehicle ID and maintenance type; financial payment reports accept payment status;
fleet inventory accepts vehicle ID. Dashboard accepts dates only; fleet
inventory accepts vehicle ID and date parameters, although its metrics are a
current snapshot. Driver summary and availability are unfiltered current
snapshots; driver performance accepts shipment filters. These current-state
counts are not reconstructed historically from the requested date range.

Analytics roles follow the controller annotations: dashboard is restricted to
`SUPER_ADMIN` and `ADMIN`; finance reports also allow `FINANCE_MANAGER`; fleet,
driver, and maintenance reports allow `FLEET_MANAGER`; shipment and delivery
reports allow `FLEET_MANAGER` and `DISPATCHER` in addition to administrators.

## Calculation rules and limits

- Revenue counts only `SUCCESS` payments, grouped by currency. Invoice totals,
  collected payments, outstanding invoices, and pending payment requests are
  separate metrics. Currencies are never summed together.
- Outstanding balance uses invoice `total_amount - amount_paid` for open invoice
  statuses. Failed payments do not count as collected revenue. Refund records
  are not included because the current billing model has no refund ledger.
- Invoiced totals and average invoice values include issued, partially paid,
  paid, and overdue invoices; drafts and cancelled invoices are excluded.
- On-time delivery compares `delivered_at` with the shipment's promised
  `expected_delivery_date`; missing timestamps or promised dates are excluded
  from the on-time denominator. Delivery duration runs from shipment creation
  to the persisted delivery timestamp. Delivery report date windows use the
  actual completion timestamp where present, then the last update time, then
  record creation time as a fallback for incomplete records.
- Vehicle assignment coverage is assigned/in-transit vehicles divided by all
  vehicles. It is a current snapshot, not an operating-hours utilization rate;
  historical availability hours are not stored.
- Maintenance records do not store currency. Their reported currency is the
  configured `fleetflow.billing.currency`; installations with mixed maintenance
  currencies need a schema change before cross-currency reporting is valid.
- Billing customer identifiers are stored as numeric IDs while the customer
  module uses UUIDs. Revenue-by-customer reporting groups by the opaque billing
  account reference, labels it as such, and never maps it to a customer name.
  Billing shipment references are
  also exposed only as opaque billing references because they have no FK to the
  UUID shipment table.
- Driver performance is derived from assigned shipments and persisted delivery
  records; no synthetic score is produced.
- Monthly operating expense currently represents maintenance cost only;
  the domain has no fuel, toll, or labor cost ledger.
- Delivery on-time status compares the persisted delivery timestamp to the
  promised shipment delivery timestamp. Records without a promise date are
  returned in a separate `PROMISE_DATE_MISSING` bucket, and unfinished records
  are excluded from completed-delivery outcome charts.
- Route records contain estimated `distance_km` but no actual odometer/GPS
  distance ledger. The delivery summary's `averageEstimatedRouteDistanceKm`
  uses that planned distance for completed deliveries with route data; it does
  not report actual distance travelled.
- The current data model has no tenant/organization identifier. Analytics
  endpoints are therefore restricted to organization-level roles and do not
  expose these aggregates to customer or driver roles.

The API uses PostgreSQL aggregate queries instead of loading entity collections.
Migration `V33` adds indexes for date-filtered shipment, delivery, and billing
aggregates; existing maintenance indexes are reused.
