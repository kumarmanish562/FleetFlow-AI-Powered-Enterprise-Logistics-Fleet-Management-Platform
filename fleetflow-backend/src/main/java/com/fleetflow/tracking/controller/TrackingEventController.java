
package com.fleetflow.tracking.controller;

import com.fleetflow.tracking.dto.CreateTrackingEventRequest;
import com.fleetflow.tracking.dto.TrackingEventResponse;
import com.fleetflow.tracking.service.TrackingEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/shipments/{shipmentId}/tracking-events")
public class TrackingEventController {

    private final TrackingEventService trackingService;

    public TrackingEventController(
            TrackingEventService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingEventResponse create(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody CreateTrackingEventRequest request) {

        return trackingService.create(shipmentId, request);
    }

    @GetMapping("/{eventId}")
    public TrackingEventResponse getById(
            @PathVariable UUID shipmentId,
            @PathVariable UUID eventId) {

        return trackingService.getById(shipmentId, eventId);
    }

    @GetMapping("/latest")
    public TrackingEventResponse getLatest(
            @PathVariable UUID shipmentId) {

        return trackingService.getLatest(shipmentId);
    }

    @GetMapping
    public Page<TrackingEventResponse> getHistory(
            @PathVariable UUID shipmentId,
            @RequestParam(defaultValue = "0")
            @Min(0) int page,
            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size) {

        return trackingService.getHistory(
                shipmentId,
                PageRequest.of(page, size)
        );
    }
}
