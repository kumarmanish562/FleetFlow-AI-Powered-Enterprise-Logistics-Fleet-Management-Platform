# FleetFlow Admin Web

React and Vite operations interface for the existing FleetFlow Spring Boot API. The application entry, router, authentication provider, theme provider, and shell are TypeScript; feature pages remain JavaScript during the incremental migration. API data uses the backend's actual contracts with no mock production data.

## Run locally

1. Copy `.env.example` to `.env` and set `VITE_API_BASE_URL` to the backend API base URL (default: `http://localhost:8080/api/v1`).
2. Start the Spring Boot backend and configure its CORS allowlist for the frontend origin.
3. Run `npm install`, then `npm run dev`.
4. Run `npm run typecheck`, `npm test`, and `npm run build` before shipping. The build includes the TypeScript check.

## Frontend implementation

- Typed React/Vite entry point, route protection, auth context, and theme context.
- Responsive mobile drawer and collapsible desktop sidebar.
- Header page search that navigates only to modules available to the signed-in role.
- Profile menu with account settings and sign-out; light/dark theme preference persists in the browser.
- Shared TanStack Query client and the existing centralized Axios/JWT refresh integration.
- Theme, navigation, sidebar, login, and authentication basics covered by the current Vitest suite.
- Responsive dashboard and role scoped analytics views, operational CRUD pages, supported shipment/vehicle/driver/customer state changes, notification preferences, and maintenance workflows.
- Shipment tracking polls the selected shipment's latest event and maps its coordinates when present. `VITE_MAP_TILE_URL` can override the OpenStreetMap tile URL; no coordinates are invented.
- Invoice creation and lookup are available to administrators; customers can initiate a payment against a known invoice. Invoice and payment registers are not exposed by the backend.
- AI assistant is represented as an explicit backend dependency because no assistant endpoint exists yet.

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
- The tracking map displays only coordinates returned by the selected shipment's tracking event. There is no fleet location feed or route geometry, so the map is not a fleet-wide live map.
- Backend CORS configuration must allow the chosen frontend development and deployment origins.

See [`docs/PHASE_15_BACKEND_DEPENDENCIES.md`](../docs/PHASE_15_BACKEND_DEPENDENCIES.md) for the contract-by-contract dependency register, including protected role mismatches and missing route, tracking, billing, and admin operations.
