
# FleetFlow — Use Cases

## UC-01 — Create User

**Actor:** ADMIN

### Preconditions

- Admin is authenticated.
- Admin has user-management permission.

### Main Flow

1. Admin opens user management.
2. Admin selects Create User.
3. Admin enters user information.
4. Admin selects a role.
5. System validates the information.
6. System creates the user.
7. System records an audit event.

### Postconditions

A new user exists in the organization.

---

# UC-02 — Register Vehicle

**Actor:** FLEET_MANAGER

### Preconditions

- User is authenticated.
- User has vehicle-management permission.

### Main Flow

1. Fleet Manager opens vehicle management.
2. Selects Add Vehicle.
3. Enters vehicle details.
4. System validates registration information.
5. System creates the vehicle.
6. Vehicle receives `AVAILABLE` status.

---

# UC-03 — Register Driver

**Actor:** FLEET_MANAGER

### Main Flow

1. Fleet Manager opens driver management.
2. Selects Add Driver.
3. Enters driver information.
4. Provides license information.
5. System validates the data.
6. Driver is created.
7. Driver becomes available for assignment.

---

# UC-04 — Create Shipment

**Actor:** DISPATCHER / CUSTOMER

### Main Flow

1. User creates shipment request.
2. System captures customer information.
3. User enters origin.
4. User enters destination.
5. User enters shipment details.
6. System validates the request.
7. Shipment is created with `CREATED` status.

---

# UC-05 — Assign Vehicle and Driver

**Actor:** DISPATCHER

### Preconditions

- Shipment exists.
- Vehicle is available.
- Driver is available.

### Main Flow

1. Dispatcher opens shipment.
2. System displays available vehicles.
3. Dispatcher selects vehicle.
4. System displays eligible drivers.
5. Dispatcher selects driver.
6. System validates assignment.
7. Shipment becomes `ASSIGNED`.

---

# UC-06 — Start Delivery

**Actor:** DRIVER

### Main Flow

1. Driver logs into FleetFlow.
2. Driver views assigned shipment.
3. Driver starts delivery.
4. System records the event.
5. Shipment changes to `IN_TRANSIT`.

---

# UC-07 — Update Shipment Status

**Actor:** DRIVER

The driver can update delivery progress.

Example:

```text
ASSIGNED
    ↓
PICKUP_PENDING
    ↓
PICKED_UP
    ↓
IN_TRANSIT
    ↓
DELIVERED
```

Every status change should be recorded as a tracking event.

---

# UC-08 — Report Incident

**Actor:** DRIVER

### Main Flow

1. Driver opens active shipment.
2. Selects Report Incident.
3. Selects incident type.
4. Provides description.
5. System creates incident.
6. Dispatcher receives notification.
7. Incident is tracked until resolution.

---

# UC-09 — Track Shipment

**Actor:** CUSTOMER

### Main Flow

1. Customer logs in.
2. Opens shipment.
3. System displays current status.
4. System displays latest tracking event.
5. Customer views delivery progress.

---

# UC-10 — Generate Invoice

**Actor:** FINANCE_MANAGER

### Main Flow

1. Finance Manager opens completed shipment.
2. Reviews shipment charges.
3. Creates invoice.
4. System calculates total amount.
5. Invoice is generated.
6. Customer receives notification.

---

# UC-11 — Schedule Maintenance

**Actor:** FLEET_MANAGER

### Main Flow

1. Fleet Manager selects vehicle.
2. Reviews maintenance history.
3. Creates maintenance schedule.
4. System marks maintenance requirement.
5. Vehicle availability is updated where necessary.

---

# UC-12 — AI Operational Assistant

**Actor:** ADMIN / FLEET_MANAGER / DISPATCHER

Example query:

```text
"Which vehicles are currently unavailable?"
```

The AI layer retrieves authorized FleetFlow information and generates a natural-language response.

Another example:

```text
"Show me shipments that are delayed today."
```

The AI assistant should retrieve relevant operational data before generating an answer.

The AI layer must respect the user's authorization.

A user must never receive data they are not permitted to access.

---

# UC-13 — View Fleet Analytics

**Actor:** FLEET_MANAGER / ADMIN

The user can view:

- Fleet utilization.
- Active vehicles.
- Maintenance costs.
- Vehicle downtime.
- Shipment performance.
- Driver performance.

---

# UC-14 — Review Financial Analytics

**Actor:** FINANCE_MANAGER / ADMIN

The user can view:

- Revenue.
- Invoice totals.
- Paid invoices.
- Outstanding balances.
- Overdue invoices.