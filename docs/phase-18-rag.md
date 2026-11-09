# Phase 18 — RAG knowledge system

FleetFlow stores private company documents in a persistent filesystem location and uses Spring AI's PGVectorStore in the existing PostgreSQL database. The AI assistant retrieves only chunks from documents authorized for the signed-in user's organization and roles. It has no operational write tools.

## Enable the service

The knowledge service is off by default. Set the following variables for the backend:

```text
FLEETFLOW_AI_ENABLED=true
FLEETFLOW_KNOWLEDGE_ENABLED=true
SPRING_AI_MODEL_CHAT=openai
SPRING_AI_MODEL_EMBEDDING=openai
OPENAI_API_KEY=<secret>
AI_MODEL=gpt-4o-mini
AI_EMBEDDING_MODEL=text-embedding-3-small
KNOWLEDGE_STORAGE_PATH=<private persistent directory>
REDIS_HOST=<redis host>
REDIS_PORT=6379
```

The Flyway migration creates `knowledge_documents`, `knowledge_jobs`, and Spring AI's `vector_store` table. V1 already enables the `vector` extension. The migration fixes the vector dimension at 1,536, matching `text-embedding-3-small`. Changing the embedding model or dimension requires a new migration and a full reindex; changing only the environment variable is unsafe. `spring.ai.vectorstore.pgvector.initialize-schema=false` keeps Flyway in control.

The configured storage directory must be private, persistent across restarts, and outside any web server's static files. Back it up together with PostgreSQL. The upload limit is 20 MB per file; accepted formats are PDF, DOCX, and UTF-8 TXT. Text extraction is limited to one million characters. PDF page numbers are retained; DOCX and TXT citations do not have page numbers. Scanned image-only PDFs require OCR and will fail ingestion.

Existing users are assigned to a bootstrap organization by V35. The server reads organization and role membership from the user database, never from upload or search requests. To provision another organization, an administrator must create an `organizations` row and assign users to it through a controlled database or identity administration workflow. FleetFlow does not yet expose a tenant provisioning API. Conversation ownership also includes organization ID, so changing a user's organization does not expose their old conversation history.

## Document processing

Uploading writes a validated private file, its checksum, document metadata, and a queued job. The bounded scheduled worker atomically claims a job with `FOR UPDATE SKIP LOCKED`, extracts and chunks text, and sends chunks to Spring AI PGVectorStore for embedding. New chunks remain inactive until all expected chunks are stored. The worker activates the new generation and document metadata in one database transaction, then removes the old generation. Failed jobs retry with backoff up to the configured limit; administrators may retry a failed job. A replacement marks its chosen older document `SUPERSEDED` only after indexing succeeds. Deletion immediately removes a document from normal retrieval, then removes vectors and its file. A periodic cleanup retries file deletion if that last step fails.

Document access changes update both relational metadata and vector metadata. Search first finds active documents allowed by organization and role, then applies their IDs, organization ID, and active flag as a filter **inside** the pgvector similarity query. The assistant passes only those results to the LLM. When search returns no authorized context, chat responds without calling the LLM. Citations include document ID, filename, version, and PDF page when available.

## API

All endpoints use the existing JWT bearer token. Document management requires `ADMIN` or `SUPER_ADMIN`; search and chat accept existing FleetFlow roles. API errors use the existing `ErrorResponse` format. Swagger UI is at `/swagger-ui/index.html`; OpenAPI JSON is at `/v3/api-docs`.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/ai/knowledge/documents` | Multipart upload, returns 202 |
| GET | `/api/v1/ai/knowledge/documents` | Organization-scoped, paged list |
| GET | `/api/v1/ai/knowledge/documents/{id}` | Document metadata |
| GET | `/api/v1/ai/knowledge/documents/{id}/status` | Document and job state |
| POST | `/api/v1/ai/knowledge/documents/{id}/reindex` | Queue a new generation, returns 202 |
| POST | `/api/v1/ai/knowledge/documents/{id}/retry` | Retry failed job, returns 202 |
| PUT | `/api/v1/ai/knowledge/documents/{id}/access` | Replace access role list |
| DELETE | `/api/v1/ai/knowledge/documents/{id}` | Queue deletion, returns 202 |
| POST | `/api/v1/ai/knowledge/search` | Authorized source search |
| POST | `/api/v1/ai/chat` | Answer with `sources` citations |

To test in Postman: log in as an administrator; upload a file with form fields `file`, `documentType`, `version`, and repeated `accessRoles` values. Optionally add `replacesDocumentId`. Poll the returned document's `/status` until `INDEXED`. Then search with `{"query":"What is the breakdown procedure?"}` and chat with `{"message":"What is the breakdown procedure?","conversationId":null}`. Confirm that a user without an allowed role gets no results, another organization's document ID returns 404, and deleting a document removes it from subsequent search. Change access with `{"accessRoles":["ADMIN","DRIVER"]}`.

## Verification

Run `npm.cmd run build` in `admin-web` and `mvnw.cmd test` in `fleetflow-backend`. PostgreSQL integration tests use the existing `pgvector/pgvector:pg18` Testcontainers image, so Docker must be running. A live model check requires a valid provider key, Redis, and PostgreSQL. Ordinary unit tests mock retrieval and the model; they do not spend API credits.
