
# FleetFlow — Requirements Specification

## 1. Functional Requirements

## FR-01 — User Management

The system shall allow authorized administrators to:

- Create users.
- Update users.
- Disable users.
- View users.
- Assign roles.
- Change user status.
- Reset user credentials.

---

## FR-02 — Role Management

The system shall support:

```text
SUPER_ADMIN
ADMIN
FLEET_MANAGER
DISPATCHER
DRIVER
CUSTOMER
FINANCE_MANAGER
```

Access shall be determined by role and permission.

---

## FR-03 — Vehicle Management

Authorized users shall be able to:

- Register vehicles.
- Update vehicle information.
- View vehicle details.
- Change vehicle status.
- Assign vehicles.
- View vehicle history.
- Record maintenance.
- Retire vehicles.

Vehicle information may include:

```text
Vehicle ID
Registration Number
Vehicle Type
Make
Model
Manufacturing Year
Capacity
Fuel Type
Current Mileage
Status
```

---

## FR-04 — Driver Management

The system shall allow authorized users to:

- Register drivers.
- Update driver profiles.
- Manage driver availability.
- Assign drivers.
- View driver history.
- Record incidents.
- View driver performance.

---

## FR-05 — Customer Management

The system shall allow authorized users to:

- Create customers.
- Update customers.
- View customers.
- Manage customer addresses.
- View shipment history.
- View billing information.

---

## FR-06 — Shipment Management

The system shall allow authorized users to:

- Create shipments.
- Update shipments.
- Cancel shipments.
- Assign vehicles.
- Assign drivers.
- Track shipment status.
- View shipment history.
- Confirm delivery.

---

## FR-07 — Dispatch Management

Dispatchers shall be able to:

- View pending shipments.
- Assign vehicles.
- Assign drivers.
- Schedule deliveries.
- Monitor active shipments.
- Handle failed deliveries.
- Handle operational exceptions.

---

## FR-08 — Route Management

The system shall support:

- Origin and destination management.
- Delivery stops.
- Route information.
- Distance.
- Estimated travel time.
- Route status.
- Route history.

Future versions may support route optimization.

---

## FR-09 — Tracking

The system shall support shipment tracking.

Tracking events may include:

```text
SHIPMENT_CREATED
DRIVER_ASSIGNED
VEHICLE_ASSIGNED
PICKUP_STARTED
PICKED_UP
IN_TRANSIT
DELAYED
ARRIVED
DELIVERED
DELIVERY_FAILED
```

---

## FR-10 — Incident Management

Drivers and authorized staff shall be able to report incidents.

Examples:

- Vehicle breakdown.
- Accident.
- Delivery failure.
- Customer unavailable.
- Route obstruction.
- Package issue.

Each incident should contain:

```text
Incident ID
Shipment ID
Vehicle ID
Driver ID
Type
Severity
Description
Created At
Resolved At
Status
```

---

## FR-11 — Maintenance Management

The system shall support:

- Maintenance schedules.
- Service records.
- Vehicle inspections.
- Maintenance status.
- Maintenance cost tracking.
- Maintenance history.

---

## FR-12 — Billing

The system shall support:

- Shipment pricing.
- Invoice generation.
- Invoice status.
- Payment records.
- Outstanding balance.
- Billing history.

Possible invoice states:

```text
DRAFT
ISSUED
PAID
PARTIALLY_PAID
OVERDUE
CANCELLED
```

---

## FR-13 — Notifications

The system shall support notifications for important operational events.

Examples:

```text
Shipment assigned
Shipment picked up
Shipment delayed
Shipment delivered
Vehicle maintenance due
Invoice generated
Payment received
Incident reported
```

---

## FR-14 — Analytics

The system shall provide analytics for authorized users.

Examples:

```text
Total Shipments
Active Shipments
Delivered Shipments
Failed Deliveries
Fleet Utilization
Vehicle Downtime
Driver Performance
Maintenance Costs
Revenue
Outstanding Payments
```

---

## FR-15 — Audit Logging

Security-sensitive and business-critical actions should be recorded.

Examples:

```text
USER_CREATED
ROLE_CHANGED
VEHICLE_CREATED
SHIPMENT_CREATED
SHIPMENT_ASSIGNED
SHIPMENT_STATUS_CHANGED
INVOICE_CREATED
PAYMENT_UPDATED
```

---

# 2. Non-Functional Requirements

## NFR-01 — Security

The platform must provide:

- Secure authentication.
- Role-based authorization.
- Password hashing.
- Token-based authentication.
- Input validation.
- Secure API design.
- Audit logging.

---

## NFR-02 — Performance

Normal API operations should respond quickly under expected system load.

Performance-sensitive operations should be monitored using metrics.

---

## NFR-03 — Scalability

The architecture should support horizontal scaling of backend services where required.

---

## NFR-04 — Availability

Core logistics functionality should remain available even if optional external services fail.

---

## NFR-05 — Maintainability

The codebase should follow:

- Clean architecture principles.
- SOLID principles.
- Consistent naming.
- Modular design.
- Automated testing.

---

## NFR-06 — Observability

The platform should provide:

- Application logs.
- Error logs.
- Metrics.
- Health checks.
- Request tracing where appropriate.

---

## NFR-07 — Data Integrity

Business-critical operations must maintain transactional consistency.

---

## NFR-08 — Testability

The platform should support:

- Unit tests.
- Integration tests.
- Repository tests.
- Controller/API tests.
- Security tests.

---

## NFR-09 — Deployment

The system should be containerized and suitable for cloud deployment.

---

# 3. Business Rules

### Vehicle Assignment

A vehicle should not be assigned to multiple active shipments where operational constraints make such assignment impossible.

### Driver Assignment

A driver should only be assigned when:

- Driver is active.
- Driver is available.
- Driver has required authorization.

### Shipment Status

Shipment transitions must follow valid business states.

For example:

```text
CREATED
   ↓
CONFIRMED
   ↓
ASSIGNED
   ↓
PICKUP_PENDING
   ↓
IN_TRANSIT
   ↓
DELIVERED
```

Invalid transitions should be rejected.

---

## 4. Constraints

FleetFlow must consider:

- Vehicle availability.
- Driver availability.
- Vehicle capacity.
- Shipment status.
- Maintenance status.
- User permissions.
- Financial status.

---

## 5. Assumptions

Initial assumptions:

- FleetFlow is organization-oriented.
- Users belong to an organization/tenant.
- Roles determine default permissions.
- Core data is stored in PostgreSQL.
- Backend APIs are exposed through REST.
- Frontend is a React application.
- AI services are optional extensions.