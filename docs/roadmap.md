
# FleetFlow — Development Roadmap

## Phase 0 — Product Planning

### Status

Completed / Foundation

### Scope

- Product vision.
- Product modules.
- User roles.
- Core use cases.
- Initial product direction.

---

# Phase 1 — Product Foundation & System Specification

### Goal

Convert the product idea into an implementation-ready specification.

### Deliverables

```text
docs/
├── product-vision.md
├── requirements.md
├── use-cases.md
├── architecture.md
├── security-model.md
└── roadmap.md
```

### Definition of Done

- [x] Product vision defined.
- [x] User roles defined.
- [x] Core domains defined.
- [x] Functional requirements defined.
- [x] Non-functional requirements defined.
- [x] Use cases documented.
- [x] Architecture direction defined.
- [x] Security model defined.
- [x] AI strategy defined.
- [x] Future roadmap defined.

---

# Phase 2 — Backend Foundation

### Goal

Create the Spring Boot backend foundation.

### Technologies

```text
Java
Spring Boot
Spring Security
Spring Data JPA
PostgreSQL
Flyway
Bean Validation
Actuator
JUnit
Testcontainers
Docker
```

### Deliverables

- Spring Boot project.
- Database connection.
- Flyway migrations.
- Base entities.
- Exception handling.
- API response structure.
- Validation.
- Security foundation.
- Test infrastructure.
- Docker environment.

---

# Phase 3 — Identity & Access Management

### Goal

Implement authentication and authorization.

### Features

```text
Registration
Login
Logout
Refresh Token
JWT
RBAC
User Management
Role Management
Permission Management
```

---

# Phase 4 — Fleet & Driver Management

### Features

```text
Vehicle CRUD
Vehicle Status
Vehicle Assignment
Driver CRUD
Driver Availability
Driver Assignment
Vehicle History
Driver History
```

---

# Phase 5 — Customer Management

### Features

```text
Customer Registration
Customer Profiles
Customer Addresses
Customer Shipment History
Customer Account Management
```

---

# Phase 6 — Shipment Management

### Features

```text
Create Shipment
Update Shipment
Cancel Shipment
Shipment Assignment
Shipment Status
Shipment History
Shipment Validation
```

---

# Phase 7 — Dispatch & Route Management

### Features

```text
Dispatch Dashboard
Vehicle Assignment
Driver Assignment
Route Creation
Route Stops
Route Status
Dispatch Exceptions
```

---

# Phase 8 — Tracking & Incident Management

### Features

```text
Tracking Events
Shipment Timeline
Driver Updates
Incident Reporting
Incident Resolution
Delivery Confirmation
```

---

# Phase 9 — Maintenance Management

### Features

```text
Maintenance Scheduling
Service Records
Maintenance History
Vehicle Inspections
Maintenance Costs
Vehicle Downtime
```

---

# Phase 11 — Event-Driven Architecture

### Status

Completed for the backend event foundation.

### Goal

Publish shipment lifecycle events asynchronously through Kafka while keeping
the shipment write and event record atomic through the transactional outbox.

Potential technology:

```text
Apache Kafka
```

### Delivered

- Versioned event envelopes with event and aggregate identifiers.
- Transactional outbox with concurrent batch claims, retry backoff, and failed-event state.
- Shipment-created, assigned, picked-up, and delivered events recorded with the shipment transaction.
- Idempotent consumer processing and dead-letter handling for notification consumers.
- Local Kafka broker in Docker Compose and configurable topic names.
- Scheduled outbox polling enabled by the backend messaging configuration.

The outbox is the source of truth for publication: a Kafka outage leaves events
pending for retry without losing the committed shipment change. Consumers should
remain idempotent because delivery is at least once.

Example:

```text
Shipment Service
      ↓
ShipmentDelivered Event
      ↓
Kafka
 ┌────┼──────┐
 ▼    ▼      ▼
Billing Analytics Notification
```

---

# Phase 12 — Billing & Finance

### Features

```text
Pricing
Invoice Generation
Invoice Management
Payment Records
Outstanding Balance
Financial Reports
```

---

# Phase 13 — Notifications

### Features

```text
In-App Notifications
Email Notifications
Shipment Alerts
Maintenance Alerts
Billing Notifications
Incident Alerts
```

Future integrations may include SMS or messaging providers.

---

# Phase 14 — Analytics & Reporting

The production analytics scope and calculation rules are documented in
[`analytics.md`](analytics.md). This phase follows billing and notifications
as listed above and uses PostgreSQL as the reporting source of truth.

### Features

```text
Fleet Dashboard
Shipment Dashboard
Driver Analytics
Maintenance Analytics
Financial Analytics
Operational KPIs
```

---

# Phase 15 — React Frontend

### Goal

Build the complete FleetFlow web application.

### Features

```text
Authentication
Dashboard
Fleet UI
Driver UI
Customer UI
Shipment UI
Dispatch UI
Tracking UI
Maintenance UI
Billing UI
Analytics UI
```

---

# Phase 16 — AI Intelligence

### Goal

Introduce Spring AI and intelligent operational capabilities.

### Technology

```text
Spring AI
LLM
Embeddings
Vector Database
RAG
Tool Calling
```

Potential capabilities:

```text
AI Fleet Assistant
Shipment Risk Analysis
Maintenance Assistant
Incident Summarization
Operational Q&A
Report Generation
```

---

# Phase 17 — AI RAG

### Goal

Build controlled retrieval-augmented generation.

```text
User
 ↓
AI Assistant
 ↓
Authorization
 ↓
Retriever
 ↓
Vector Store
 ↓
Relevant Context
 ↓
LLM
 ↓
Response
```

Potential knowledge sources:

```text
Company Policies
Fleet Documentation
Maintenance Manuals
Operational Procedures
Shipment Policies
Internal Documentation
```

---

# Phase 18 — DevOps & Cloud

### Goal

Prepare FleetFlow for production deployment.

### Technologies

```text
Docker
Docker Compose
GitHub Actions
AWS
Terraform
Kubernetes
Prometheus
Grafana
```

Potential AWS services:

```text
EC2 / ECS
RDS PostgreSQL
S3
CloudWatch
ECR
ALB
IAM
Secrets Manager
```

---

# Phase 19 — Production Hardening

### Focus

- Security testing.
- Performance testing.
- Load testing.
- Database optimization.
- API optimization.
- Observability.
- Disaster recovery.
- Backup strategy.
- CI/CD.
- Documentation.

---

# Phase 20 — Final Enterprise Release

FleetFlow should eventually provide:

```text
                    FleetFlow
                        │
       ┌────────────────┼────────────────┐
       │                │                │
   Operations        Finance            AI
       │                │                │
 Fleet Management    Billing       AI Assistant
 Driver Management   Payments      RAG
 Shipments           Analytics     Predictions
 Dispatch
 Tracking
 Maintenance
```

---

# Overall Development Path

```text
Phase 0
Product Planning
      ↓
Phase 1
Product Foundation
      ↓
Phase 2
Backend Foundation
      ↓
Phase 3
Security / IAM
      ↓
Phase 4
Fleet + Drivers
      ↓
Phase 5
Customers
      ↓
Phase 6
Shipments
      ↓
Phase 7
Dispatch + Routes
      ↓
Phase 8
Tracking + Incidents
      ↓
Phase 9
Maintenance
      ↓
Phase 11
Kafka / Events
      ↓
Phase 12
Billing
      ↓
Phase 13
Notifications
      ↓
Phase 14
Analytics
      ↓
Phase 15
React Frontend
      ↓
Phase 16
Spring AI
      ↓
Phase 17
RAG
      ↓
Phase 18
DevOps / AWS
      ↓
Phase 19
Production Hardening
      ↓
Phase 20
Enterprise Release
```
