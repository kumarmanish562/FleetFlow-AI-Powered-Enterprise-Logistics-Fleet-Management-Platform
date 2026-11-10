#!/usr/bin/env python3
"""Evaluate FleetFlow AI retrieval and answer behavior against a labeled JSON dataset."""

import argparse
import json
import os
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path


def request_json(base_url: str, token: str, path: str, payload: dict) -> dict | list:
    body = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        f"{base_url.rstrip('/')}{path}",
        data=body,
        headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json", "Accept": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            return json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        message = error.read().decode("utf-8", errors="replace")[:500]
        raise RuntimeError(f"{path} returned HTTP {error.code}: {message}") from error


def load_cases(path: Path) -> list[dict]:
    cases = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(cases, list) or not cases:
        raise ValueError("Dataset must be a non-empty JSON array")
    for index, case in enumerate(cases):
        if not isinstance(case, dict) or not isinstance(case.get("question"), str) or not case["question"].strip():
            raise ValueError(f"Case {index} must have a non-empty question")
        if not isinstance(case.get("answerable"), bool):
            raise ValueError(f"Case {index} must set answerable to true or false")
        for key in ("expectedDocumentIds", "forbiddenDocumentIds"):
            if key in case and not isinstance(case[key], list):
                raise ValueError(f"Case {index} field {key} must be an array")
        if case["answerable"] and not case.get("expectedDocumentIds"):
            raise ValueError(f"Answerable case {index} needs expectedDocumentIds")
    return cases


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("dataset", type=Path, help="Labeled JSON array of evaluation questions")
    parser.add_argument("--top-k", type=int, default=10)
    parser.add_argument("--retrieval-only", action="store_true", help="Skip chat generation and no-answer behavior checks")
    args = parser.parse_args()
    if args.top_k < 1 or args.top_k > 20:
        parser.error("--top-k must be between 1 and 20")

    token = os.environ.get("FLEETFLOW_TOKEN")
    if not token:
        parser.error("Set FLEETFLOW_TOKEN to a JWT for the user whose access should be evaluated")
    base_url = os.environ.get("FLEETFLOW_API_BASE_URL", "http://localhost:8080/api/v1")
    cases = load_cases(args.dataset)
    totals = {"precision": 0.0, "recall": 0.0, "recall_total": 0,
              "citation_correct": 0, "citation_total": 0, "no_answer_correct": 0,
              "no_answer_total": 0, "unauthorized_hits": 0, "unauthorized_cases": 0,
              "case_count": len(cases)}
    details = []

    for case in cases:
        sources = request_json(base_url, token, "/ai/knowledge/search", {"query": case["question"]})
        if not isinstance(sources, list):
            raise RuntimeError("Knowledge search response was not an array")
        sources = sources[:args.top_k]
        retrieved_ids = {source.get("documentId") for source in sources}
        expected = set(case.get("expectedDocumentIds", []))
        forbidden = set(case.get("forbiddenDocumentIds", []))
        relevant = len(retrieved_ids & expected)
        totals["precision"] += relevant / args.top_k
        if expected:
            totals["recall"] += len(retrieved_ids & expected) / len(expected)
            totals["recall_total"] += 1
        unauthorized = retrieved_ids & forbidden
        totals["unauthorized_hits"] += len(unauthorized)
        totals["unauthorized_cases"] += int(bool(unauthorized))

        citation_ok = None
        answer_status = None
        if not args.retrieval_only:
            answer = request_json(base_url, token, "/ai/chat", {"conversationId": None, "message": case["question"]})
            answer_status = answer.get("status")
            if case["answerable"]:
                answer_sources = answer.get("sources", [])
                references = {int(number) for number in re.findall(r"\[(\d+)]", answer.get("answer", ""))}
                cited_sources = [answer_sources[number - 1] for number in references
                                 if 1 <= number <= len(answer_sources)]
                cited_ids = {source.get("documentId") for source in cited_sources}
                expected_pages = case.get("expectedPages", {})
                pages_match = all(source.get("pageNumber") in expected_pages[str(source.get("documentId"))]
                                  for source in cited_sources if str(source.get("documentId")) in expected_pages)
                citation_ok = bool(references) and len(cited_sources) == len(references) \
                    and bool(cited_ids & expected) and cited_ids <= expected and pages_match
                totals["citation_total"] += 1
                totals["citation_correct"] += int(citation_ok)
            else:
                totals["no_answer_total"] += 1
                correct = answer_status == "INSUFFICIENT_EVIDENCE" and not answer.get("sources")
                totals["no_answer_correct"] += int(correct)

        details.append({"id": case.get("id"), "retrievedDocumentIds": sorted(i for i in retrieved_ids if i),
                        "relevantDocumentCount": relevant, "unauthorizedDocumentIds": sorted(retrieved_ids & forbidden),
                        "answerStatus": answer_status, "citationCorrect": citation_ok})

    count = totals["case_count"]
    report = {
        "caseCount": count,
        "precisionAtK": round(totals["precision"] / count, 4),
        "recallAtK": round(totals["recall"] / totals["recall_total"], 4) if totals["recall_total"] else None,
        "citationCorrectness": round(totals["citation_correct"] / totals["citation_total"], 4) if totals["citation_total"] else None,
        "noAnswerAccuracy": round(totals["no_answer_correct"] / totals["no_answer_total"], 4) if totals["no_answer_total"] else None,
        "unauthorizedRetrievalCount": totals["unauthorized_hits"],
        "unauthorizedRetrievalRate": round(totals["unauthorized_cases"] / count, 4),
        "details": details,
    }
    print(json.dumps(report, indent=2))
    return 1 if totals["unauthorized_hits"] else 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, RuntimeError) as error:
        print(f"RAG evaluation failed: {error}", file=sys.stderr)
        sys.exit(2)
