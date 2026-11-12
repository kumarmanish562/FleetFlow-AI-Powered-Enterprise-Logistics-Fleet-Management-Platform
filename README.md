
# 🚚 FleetFlow

### AI-Powered Enterprise Logistics & Fleet Management Platform

FleetFlow is a modern enterprise logistics and fleet management platform designed to manage vehicles, drivers, customers, shipments, routes, tracking, maintenance, billing, notifications, analytics, and AI-powered operational intelligence from a unified system.

The platform is being designed using a scalable backend architecture with **Java, Spring Boot, PostgreSQL, Kafka, Docker, React, Spring AI, and RAG**.

---

## 🎯 Vision

FleetFlow aims to provide a centralized platform for organizations to manage their complete logistics lifecycle.

```text
Customers
    ↓
Shipments
    ↓
Dispatch
    ↓
Vehicles + Drivers
    ↓
Routes
    ↓
Tracking
    ↓
Delivery
    ↓
Billing
    ↓
Analytics
    ↓
AI Intelligence
```

---

## 🧩 Core Modules

```text
FleetFlow
│
├── 👤 Identity & Access Management
├── 🚛 Fleet Management
├── 👨‍✈️ Driver Management
├── 🏢 Customer Management
├── 📦 Shipment Management
├── 🗺️ Route Management
├── 📍 Tracking
├── 🔧 Maintenance
├── 💰 Billing
├── 🔔 Notifications
├── 📊 Analytics
└── 🤖 AI Intelligence
```

---

## 👥 User Roles

```text
SUPER_ADMIN
ADMIN
FLEET_MANAGER
DISPATCHER
DRIVER
CUSTOMER
FINANCE_MANAGER
```

---

## 🛠️ Technology Direction

### Backend

```text
Java
Spring Boot
Spring Security
Spring Data JPA
PostgreSQL
Flyway
Kafka
Spring AI
```

### Frontend

```text
React
TypeScript
Vite
Tailwind CSS
```

### Infrastructure

```text
Docker
Docker Compose
GitHub Actions
AWS
Terraform
Kubernetes
```

### Observability

```text
Spring Boot Actuator
Micrometer
Prometheus
Grafana
```

---

## 🤖 AI Intelligence

The informational Spring AI backend foundation, its environment configuration, and API details are documented in [Phase 17 — Spring AI](docs/phase-17-spring-ai.md).

The document ingestion, secure retrieval, citations, and setup for Phase 18 are documented in [Phase 18 — RAG knowledge system](docs/phase-18-rag.md).

AI is designed as an intelligence layer over the FleetFlow platform.

Potential capabilities include:

- AI Fleet Assistant.
- Natural-language operational queries.
- Shipment risk analysis.
- Maintenance recommendations.
- Incident summarization.
- Automated operational reports.
- Document understanding.
- RAG-powered knowledge assistant.

The core FleetFlow system does not depend on AI to perform fundamental logistics operations.

---

## 🏗️ Architecture Direction

```text
React Frontend
      │
      ▼
API Gateway
      │
      ▼
Spring Boot Backend
      │
 ┌────┼─────────────────┐
 ▼    ▼                 ▼
Fleet Shipment       Billing
 │      │                │
 └──────┼────────────────┘
        ▼
    PostgreSQL
        │
        ▼
      Kafka
        │
        ▼
 AI Intelligence
        │
   Spring AI + RAG
        │
        ▼
       LLM
```

---

## 📁 Repository Structure

```text
fleetflow-platform/
│
├── backend/
├── frontend/
├── infrastructure/
│
├── docs/
│   ├── product-vision.md
│   ├── requirements.md
│   ├── use-cases.md
│   ├── architecture.md
│   ├── security-model.md
│   └── roadmap.md
│
├── .github/
│
├── .gitignore
└── README.md
```

---

## 🗺️ Development Roadmap

```text
Phase 0  → Product Planning
Phase 1  → Product Foundation
Phase 2  → Backend Foundation
Phase 3  → IAM / Security
Phase 4  → Fleet + Drivers
Phase 5  → Customers
Phase 6  → Shipments
Phase 7  → Dispatch + Routes
Phase 8  → Tracking + Incidents
Phase 9  → Maintenance
Phase 11 → Kafka / Event Architecture
Phase 12 → Billing
Phase 13 → Notifications
Phase 14 → Analytics & Reporting
Phase 15 → React Frontend
Phase 16 → Spring AI
Phase 17 → RAG
Phase 18 → DevOps / AWS
Phase 19 → Production Hardening
Phase 20 → Enterprise Release
```

---

## 📚 Documentation

Detailed project documentation is maintained under:

```text
docs/
```

### Product

- `product-vision.md`
- `requirements.md`
- `use-cases.md`

### Architecture

- `architecture.md`
- `security-model.md`

### Planning

- `roadmap.md`
- `analytics.md`

---

## 🚧 Project Status

```text
Phase 13 — Notifications
           ✅ Completed

Phase 14 — Analytics & Reporting backend
           🟡 Core implementation in progress; PostgreSQL integration verification pending

Phase 15 — React Frontend
           ⏳ Upcoming

Phase 20 — AI Operational Intelligence
           ✅ Tenant-scoped read-only tools, customer grants, grounded evidence, incidents, analytics and Kafka context implemented (see [Phase 20 status](docs/phase-20-ai-operational-intelligence.md)).
```

---

## 🎯 Project Objective

FleetFlow is being developed as a portfolio-grade enterprise application demonstrating practical knowledge of:

- Java backend development.
- Spring Boot.
- Spring Security.
- REST API design.
- PostgreSQL.
- Database migrations.
- Event-driven architecture.
- Kafka.
- React.
- Docker.
- DevOps.
- AWS.
- Spring AI.
- RAG.
- Enterprise security.
- Observability.

---

## 📌 Repository

**Recommended name:**

```text
fleetflow-platform
```

**Short description:**

```text
AI-powered enterprise logistics and fleet management platform built with Spring Boot, React, PostgreSQL, Kafka, Docker, and Spring AI.
```
