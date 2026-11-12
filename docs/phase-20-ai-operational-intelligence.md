# Phase 20 — AI Operational Intelligence

## Implemented

- The existing Phase 19 chat and history stay in place. `POST /api/v1/ai/operational/query` accepts `question` and returns a structured operational answer.
- Spring AI is configured with read-only tools for vehicle status and unavailable vehicles, available drivers and one driver's availability, shipment detail and latest tracking event, recent maintenance, reported incidents, and route delay analytics. Tools call scoped domain services and return bounded DTOs.
- Tool calls resolve organization and role from the authenticated principal for every invocation. Customer accounts receive only shipment and incident tools when an administrator has granted a customer-to-user association. The model cannot submit an organization or customer scope.
- `V39`–`V43` add organization ownership to customer and fleet assets, derive the organization for related shipment, route, tracking, delivery, maintenance and driver records, and reject cross-organization relationships at the database level. Existing cross-organization assignments encountered during migration are cleared for later reassignment; the records remain.
- Admins can grant or revoke customer AI access at `/api/v1/ai/operational/customer-access`. Grants require the customer and account to belong to the admin's organization and the account to have the `CUSTOMER` role.
- Operational tools produce a durable audit record with organization, user, tool and outcome. The audit keeps one year of metadata and stores no tool arguments or response payloads.
- Incident reporting and resolution are available through `POST /api/v1/incidents` and `PATCH /api/v1/incidents/{id}/resolve`. Incidents are tenant-scoped, linked to an authorized shipment, and published through the transactional outbox. The AI tool returns a sanitized summary without reporter details.
- Fleet status, delivery outcomes and route delay analytics are calculated in PostgreSQL through organization-scoped queries. Date-bounded analytics accept up to 90 days in UTC. A delivery is counted as delayed when its actual delivery time is later than its promised delivery time. Route reports group up to 20 routes and use delivered time for the reporting period.
- Kafka stores allow-listed event metadata only, with organization resolution, idempotency, retries, a dedicated dead-letter topic and a 90-day retention window. PostgreSQL domain records remain the source of current state.
- Operational evidence includes the tool name, source type, retrieval timestamp and bounded source record IDs. Tool references, explanations, recommendations and authorized document citations are returned as structured fields and persisted for conversation reload. Recommendations are returned only with a valid citation to an authorized retrieved document.
- The existing React assistant shows the same evidence and citations for new and reopened conversations.

## Security boundary

AI tool queries are organization-scoped. Customer shipment and incident access requires an administrator-created mapping; email equality alone does not grant access. Fleet, driver, maintenance and analytics tools are restricted to internal operations roles. Operational AI never accepts caller-provided tenant IDs and has no write tools.

The tenant migration adds database constraints for linked records. Any pre-existing links found to cross organization boundaries are nulled by `V43` so the migration can preserve the underlying records without retaining an unsafe assignment.

## Remaining platform-wide work

The AI read path and the domain writes used by these tools are tenant-aware. Some pre-existing non-AI domain REST methods still use global repository operations. Extending tenant authorization to every existing FleetFlow endpoint is a separate platform-wide hardening task and is not represented as complete by this Phase 20 status.

The event context is an event metadata timeline, not a historical projection of full business payloads. The current source-of-truth lookup remains PostgreSQL. The route delay definition intentionally counts only delivered shipments with a valid promised date when evaluating lateness.

## Verification status

The frontend production build has passed. The focused AI/RAG and analytics unit suite passes 39 tests, including tool-policy, customer grant, evidence reload, event idempotency, date limits, and UTC analytics windows. A disposable local PostgreSQL run applied all migrations through V46 and confirmed the Phase 20 tables, tenant columns and composite constraints. The test copy omitted the pre-existing pgvector extension/table setup in V1/V35 because the installed PostgreSQL lacks pgvector; the actual repository migrations V38–V46 were used unchanged. The cross-organization shipment/vehicle assignment check was rejected by PostgreSQL as expected. V32 was corrected to avoid recreating the notification index already introduced in V16. The repository now includes PostgreSQL schema and tenant-boundary integration tests, but Docker is unavailable here, so the Testcontainers suite could not run. No application database was modified. Run the Testcontainers suite in an environment with Docker and pgvector before production release.

See `docs/roadmap.md` for the overall phase plan.
