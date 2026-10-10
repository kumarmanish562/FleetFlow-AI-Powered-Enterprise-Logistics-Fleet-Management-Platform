package com.fleetflow.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteDeliveryRequest(

        @NotBlank(message = "Recipient name is required")
        @Size(max = 200)
        String recipientName,

        @Size(max = 2000)
        String deliveryNotes,

        @Size(max = 2048)
        String proofOfDeliveryUrl

) {
}