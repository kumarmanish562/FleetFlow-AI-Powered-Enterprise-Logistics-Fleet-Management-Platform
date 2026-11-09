# Phase 17 — Spring AI foundation

The AI module provides informational chat only. It has no tools, operational repositories, SQL access, or write access to FleetFlow business records. Conversation and message persistence is isolated to `ai_conversations` and `ai_messages`.

## Runtime configuration

The module is off by default. To enable it, provide these environment variables to the backend process:

```text
FLEETFLOW_AI_ENABLED=true
SPRING_AI_MODEL_CHAT=openai
OPENAI_API_KEY=<secret from your secret manager>
AI_MODEL=gpt-4o-mini
AI_MAX_OUTPUT_TOKENS=500
AI_MAX_MESSAGE_LENGTH=2000
AI_MAX_REQUESTS_PER_MINUTE=10
AI_CONVERSATION_HISTORY_LIMIT=10
REDIS_HOST=localhost
REDIS_PORT=6379
```

`AI_MAX_MESSAGE_LENGTH` may lower the built-in 2,000 character ceiling; it cannot raise it.

Do not commit API keys. The OpenAI provider is intentionally not activated unless `SPRING_AI_MODEL_CHAT=openai` is set. Spring AI 2.0.2 is used because the backend currently uses Spring Boot 4.1.x; keep both versions aligned when upgrading.

Start the local Redis service with `docker compose -f infrastructure/docker/docker-compose.yml up -d redis`. The rate limiter uses an atomic Redis counter and fails closed if Redis is unavailable. Production deployments should use a managed Redis service with TLS, authentication, and network access controls.

Flyway migration V34 creates the two conversation tables. Conversation ownership uses the authenticated FleetFlow email identity, and missing or foreign-owned conversation IDs return the same 404 response. Chat requires authentication through the existing security filter chain.

## Endpoint

`POST /api/v1/ai/chat` accepts `{ "message": "...", "conversationId": null }`. Omit or null the ID to create a conversation; pass a returned ID to continue it. Error responses use FleetFlow's existing `ErrorResponse` structure. Provider exceptions are not returned to clients.

The endpoint authorizes the FleetFlow roles currently defined by the project. Change that explicit allowlist when the product policy for AI access changes.
