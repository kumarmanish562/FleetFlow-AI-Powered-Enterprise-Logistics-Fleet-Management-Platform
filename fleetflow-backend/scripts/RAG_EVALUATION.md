# FleetFlow RAG evaluation

Use `evaluate_rag.py` against a running FleetFlow API, with the knowledge corpus indexed and an authenticated JWT for the role being evaluated. Run the same dataset separately for each important role because retrieval access is role scoped.

The dataset is a JSON array. Use real document UUIDs and pages from the organization being evaluated; do not put policy text or customer data in source control.

```json
[
  {
    "id": "case-001",
    "question": "A question with a known answer in the indexed corpus",
    "answerable": true,
    "expectedDocumentIds": ["replace-with-document-uuid"],
    "expectedPages": {"replace-with-document-uuid": [2, 3]},
    "forbiddenDocumentIds": ["replace-with-a-document-this-role-cannot-read"]
  },
  {
    "id": "case-002",
    "question": "A question the indexed corpus cannot answer",
    "answerable": false,
    "expectedDocumentIds": [],
    "forbiddenDocumentIds": ["replace-with-a-document-this-role-cannot-read"]
  }
]
```

Example invocation (PowerShell):

```powershell
$env:FLEETFLOW_TOKEN = '<short-lived JWT>'
$env:FLEETFLOW_API_BASE_URL = 'http://localhost:8080/api/v1'
python .\scripts\evaluate_rag.py .\rag-evaluation.json
```

The runner reports precision@K, recall@K, citation correctness, no-answer accuracy, and unauthorized retrieval count. Full mode calls `/ai/chat` and creates normal conversations, so use a non-production environment and a dedicated evaluation account. `--retrieval-only` skips answer generation. Any unauthorized retrieval makes the process exit non-zero. Keep the labeled dataset and reports in an access-controlled location.
