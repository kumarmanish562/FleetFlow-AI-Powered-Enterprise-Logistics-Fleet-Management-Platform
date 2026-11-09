package com.fleetflow.ai.knowledge.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AccessUpdateRequest(@NotEmpty List<String> accessRoles) {}
