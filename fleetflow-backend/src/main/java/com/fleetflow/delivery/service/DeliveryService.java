package com.fleetflow.delivery.service;

import com.fleetflow.delivery.dto.CompleteDeliveryRequest;
import com.fleetflow.delivery.dto.DeliveryResponse;
import com.fleetflow.delivery.dto.FailDeliveryRequest;
import com.fleetflow.delivery.entity.Delivery;
import com.fleetflow.delivery.enums.DeliveryStatus;
import com.fleetflow.delivery.repository.DeliveryRepository;
import com.fleetflow.route.entity.Route;
import com.fleetflow.route.enums.RouteStatus;
import com.fleetflow.route.repository.RouteRepository;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.repository.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;

    @Transactional
    public DeliveryResponse createForShipment(
            UUID shipmentId,
            OffsetDateTime scheduledAt) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipment not found: " + shipmentId
                ));

        if (deliveryRepository.existsByShipment_Id(shipmentId)) {
            throw new IllegalStateException(
                    "A delivery already exists for this shipment"
            );
        }

        Delivery delivery = Delivery.builder()
                .shipment(shipment)
                .scheduledAt(scheduledAt)
                .status(DeliveryStatus.PENDING)
                .build();

        return toResponse(deliveryRepository.save(delivery));
    }

    public DeliveryResponse getDelivery(UUID deliveryId) {
        return toResponse(findDelivery(deliveryId));
    }

    @Transactional
    public DeliveryResponse startDelivery(UUID deliveryId) {

        Delivery delivery = findDelivery(deliveryId);
        requireStatus(delivery, DeliveryStatus.PENDING);

        Route route = routeRepository
                .findByShipment_Id(delivery.getShipment().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Route not found for shipment"
                ));

        if (route.getStatus() != RouteStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Route must be ACTIVE before delivery starts"
            );
        }

        delivery.setStatus(DeliveryStatus.IN_PROGRESS);

        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse completeDelivery(
            UUID deliveryId,
            CompleteDeliveryRequest request) {

        Delivery delivery = findDelivery(deliveryId);
        requireStatus(delivery, DeliveryStatus.IN_PROGRESS);

        Route route = routeRepository
                .findByShipment_Id(delivery.getShipment().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Route not found for shipment"
                ));

        if (route.getStatus() != RouteStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Route must be ACTIVE before delivery completion"
            );
        }

        delivery.setRecipientName(request.recipientName().trim());
        delivery.setDeliveryNotes(request.deliveryNotes());
        delivery.setProofOfDeliveryUrl(request.proofOfDeliveryUrl());
        delivery.setDeliveredAt(OffsetDateTime.now());
        delivery.setStatus(DeliveryStatus.DELIVERED);

        /*
         * REQUIRED BEFORE PRODUCTION:
         * Invoke the existing ShipmentService to transition the shipment
         * to DELIVERED in this same transaction, using the real lifecycle
         * rules and any required authorization or completion checks.
         */

        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse failDelivery(
            UUID deliveryId,
            FailDeliveryRequest request) {

        Delivery delivery = findDelivery(deliveryId);

        if (delivery.getStatus() == DeliveryStatus.DELIVERED
                || delivery.getStatus() == DeliveryStatus.FAILED) {
            throw new IllegalStateException(
                    "A terminal delivery cannot be failed again"
            );
        }

        delivery.setDeliveryNotes(request.failureReason().trim());
        delivery.setStatus(DeliveryStatus.FAILED);

        /*
         * REQUIRED BEFORE PRODUCTION:
         * Coordinate with the existing shipment failure/retry workflow
         * and record the acting user and failure event.
         */

        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse rescheduleDelivery(
            UUID deliveryId,
            OffsetDateTime newScheduledAt) {

        if (newScheduledAt == null
                || !newScheduledAt.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException(
                    "Scheduled time must be in the future"
            );
        }

        Delivery delivery = findDelivery(deliveryId);

        if (delivery.getStatus() == DeliveryStatus.DELIVERED
                || delivery.getStatus() == DeliveryStatus.FAILED) {
            throw new IllegalStateException(
                    "Terminal deliveries cannot be rescheduled"
            );
        }

        delivery.setScheduledAt(newScheduledAt);
        delivery.setStatus(DeliveryStatus.RESCHEDULED);

        return toResponse(deliveryRepository.save(delivery));
    }

    private Delivery findDelivery(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Delivery not found: " + id
                ));
    }

    private void requireStatus(
            Delivery delivery,
            DeliveryStatus expected) {

        if (delivery.getStatus() != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + " but found "
                            + delivery.getStatus()
            );
        }
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getShipment().getId(),
                delivery.getStatus(),
                delivery.getScheduledAt(),
                delivery.getDeliveredAt(),
                delivery.getRecipientName(),
                delivery.getDeliveryNotes(),
                delivery.getProofOfDeliveryUrl()
        );
    }
}