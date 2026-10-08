package com.fleetflow.auth.dto;

import java.util.Set;
import java.util.UUID;

public record UserResponse(

        UUID id,

        String email,

        String firstName,

        String lastName,

        String phone,

        String status,

        Set<String> roles
) {
}