package com.fleetflow.ai.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CustomerAccessGrantRequest(@NotNull UUID userId, @NotNull UUID customerId) {}
