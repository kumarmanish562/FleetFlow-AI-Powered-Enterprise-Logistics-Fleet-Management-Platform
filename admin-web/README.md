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

- Authentication: login, current user, refresh, logout.
- Analytics: Phase 14 dashboard, fleet, shipments, drivers, financial, delivery summaries, and shipment trend chart.
- Fleet, drivers, customers, shipments, routes: paginated list endpoints.
- Tracking: selected shipment's latest tracking event, polled every 30 seconds.
- Maintenance: vehicle selection, per-vehicle history, start and cancel transitions.
- Notifications: list, unread count, read one, read all.
- Settings: get and update notification preferences.

## Backend dependencies and current limits

- Billing needs list/search endpoints for invoices and payments. Existing APIs create and fetch a record by ID; this UI intentionally does not fabricate a register.
- User administration needs protected list, update-role, and deactivate endpoints. Only current-user auth endpoints exist today.
- Maintenance history is exposed per vehicle, not as a fleet-wide list.
- Tracking is exposed per shipment, not as an all-active-shipments location feed. The tracking page selects one shipment and polls its latest event.
- Route and shipment list APIs expose pagination and supported filters, but not every filter/action in the Phase 15 brief. The UI does not offer unsupported status transitions.
- Backend CORS configuration must allow the chosen frontend development and deployment origins.
