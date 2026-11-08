# FleetFlow Admin Web

React and Vite admin interface for the existing FleetFlow Spring Boot API. This frontend uses the backend's actual route and DTO contracts and does not include a mock data layer.

## Run locally

1. Copy `.env.example` to `.env` and set `VITE_API_BASE_URL` to the backend API base URL (default: `http://localhost:8080/api/v1`).
2. Start the Spring Boot backend and configure its CORS allowlist for the frontend origin.
3. Run `npm install`, then `npm run dev`.
4. Run `npm test` and `npm run build` before shipping.

## Authentication

The backend currently returns access and refresh tokens in the JSON response to `POST /auth/login`; it does not set HttpOnly authentication cookies. The frontend therefore stores the token pair in tab-scoped `sessionStorage`, refreshes a 401 once through `POST /auth/refresh`, loads the user from `GET /auth/me`, and clears the local session after refresh failure or logout. Backend role checks remain authoritative.

## Integrated API areas

- Authentication: login, current user, refresh, logout; token pair is kept in `sessionStorage` because the API returns it in JSON.
- Dashboard and analytics: backend summary endpoints, shipment trend, fleet/shipment/driver/customer/financial/delivery data as allowed by the signed-in role.
- Fleet, drivers, customers, shipments, routes: server-paginated lists, status filters, record details, and create forms where supported by the controller DTOs.
- Tracking: selected shipment latest event, polled every 30 seconds; there is no fleet-wide location feed or WebSocket endpoint in this backend.
- Maintenance: create, per-vehicle history, start/complete/cancel transitions, and backend upcoming/overdue summaries. There is no all-status fleet-wide register endpoint.
- Billing: invoice lookup by ID and line item display. Invoice/payment list endpoints are absent, so the page cannot provide a register.
- Notifications: list, unread count, read one, read all.
- Settings: get and update notification preferences.
- User management: shown as an explicit backend dependency; only `/auth/me` is available, with no user list or administrative mutation endpoint.

## Backend dependencies and current limits

- Billing needs list/search endpoints for invoices and payments. Existing APIs create and fetch a record by ID; this UI intentionally does not fabricate a register.
- User administration needs protected list, update-role, and deactivate endpoints. Only current-user auth endpoints exist today.
- Maintenance history is exposed per vehicle, not as a fleet-wide list.
- Tracking is exposed per shipment, not as an all-active-shipments location feed. The tracking page selects one shipment and polls its latest event.
- Route and shipment list APIs expose pagination and supported filters, but not every filter/action in the Phase 15 brief. The UI does not offer unsupported status transitions.
- Driver creation requires an existing user ID. User provisioning and administrative role operations need backend support.
- This frontend does not implement a map provider because the backend has no fleet location feed, route geometry, or provider configuration. Coordinates are displayed only when returned by tracking events.
- Backend CORS configuration must allow the chosen frontend development and deployment origins.

See [`docs/PHASE_15_BACKEND_DEPENDENCIES.md`](../docs/PHASE_15_BACKEND_DEPENDENCIES.md) for the contract-by-contract dependency register, including protected role mismatches and missing route, tracking, billing, and admin operations.
