package com.fleetflow.delivery.controller;

import com.fleetflow.delivery.dto.CompleteDeliveryRequest;
import com.fleetflow.delivery.dto.DeliveryResponse;
import com.fleetflow.delivery.dto.FailDeliveryRequest;
import com.fleetflow.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Validated
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/{id}")
    public DeliveryResponse getDelivery(@PathVariable UUID id) {
        return deliveryService.getDelivery(id);
    }

    @PatchMapping("/{id}/start")
    public DeliveryResponse startDelivery(@PathVariable UUID id) {
        return deliveryService.startDelivery(id);
    }

    @PatchMapping("/{id}/complete")
    public DeliveryResponse completeDelivery(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteDeliveryRequest request) {
        return deliveryService.completeDelivery(id, request);
    }

    @PatchMapping("/{id}/fail")
    public DeliveryResponse failDelivery(
            @PathVariable UUID id,
            @Valid @RequestBody FailDeliveryRequest request) {
        return deliveryService.failDelivery(id, request);
    }

    @PatchMapping("/{id}/reschedule")
    public DeliveryResponse rescheduleDelivery(
            @PathVariable UUID id,
            @RequestParam
            @NotNull
            @Future
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime scheduledAt) {
        return deliveryService.rescheduleDelivery(id, scheduledAt);
    }
}