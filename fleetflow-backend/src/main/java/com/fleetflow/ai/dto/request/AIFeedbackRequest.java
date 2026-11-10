package com.fleetflow.ai.dto.request;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record AIFeedbackRequest(@Pattern(regexp="POSITIVE|NEGATIVE") String type, @Size(max=1000) String comment) {}
