package com.fleetflow.ai.dto.response;

import java.time.Instant;
import java.util.List;

/** A bounded operational tool result with explicit source record provenance. */
public record OperationalFact(String label, String value, String source, Instant observedAt,
                               List<String> referenceIds) {
    public OperationalFact {
        referenceIds = referenceIds == null ? List.of() : List.copyOf(referenceIds);
    }

    public OperationalFact(String label, String value, String source, Instant observedAt) {
        this(label, value, source, observedAt, List.of());
    }
}
