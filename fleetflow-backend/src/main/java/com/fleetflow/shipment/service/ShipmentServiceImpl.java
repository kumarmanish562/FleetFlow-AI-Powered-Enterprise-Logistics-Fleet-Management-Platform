
package com.fleetflow.shipment.service;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.driver.repository.DriverRepository;
import com.fleetflow.messaging.event.shipment.ShipmentAssignedEvent;
import com.fleetflow.messaging.event.shipment.ShipmentCreatedEvent;
import com.fleetflow.messaging.event.shipment.ShipmentDeliveredEvent;
import com.fleetflow.messaging.event.shipment.ShipmentPickedUpEvent;
import com.fleetflow.messaging.outbox.OutboxService;
import com.fleetflow.shipment.dto.ShipmentAssignmentRequest;
import com.fleetflow.shipment.dto.ShipmentCreateRequest;
import com.fleetflow.shipment.dto.ShipmentResponse;
import com.fleetflow.shipment.dto.ShipmentStatusUpdateRequest;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.mapper.ShipmentMapper;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.vehicle.repository.VehicleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private static final List<ShipmentStatus> ACTIVE_STATUSES = List.of(
            ShipmentStatus.PICKUP_SCHEDULED,
            ShipmentStatus.PICKED_UP,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.OUT_FOR_DELIVERY
    );

    private final OutboxService outboxService;
    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentMapper shipmentMapper;

    public ShipmentServiceImpl(
            OutboxService outboxService,
            ShipmentRepository shipmentRepository,
            CustomerRepository customerRepository,
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            ShipmentMapper shipmentMapper
    ) {
        this.outboxService = outboxService;
        this.shipmentRepository = shipmentRepository;
        this.customerRepository = customerRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.shipmentMapper = shipmentMapper;
    }

    @Override
    @Transactional
    public ShipmentResponse create(ShipmentCreateRequest request) {

        Customer customer = customerRepository.findById(
                request.customerId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer not found"
        ));

        if (request.expectedDeliveryDate() != null
                && !request.expectedDeliveryDate()
                .isAfter(OffsetDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expected delivery must be in the future"
            );
        }

        Shipment shipment = shipmentMapper.toEntity(
                request,
                customer,
                generateShipmentNumber()
        );

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Phase 13: commit the event record atomically with the shipment.
        outboxService.record(
                outboxService.shipmentTopic(),
                "ShipmentCreated",
                "Shipment",
                savedShipment.getId().toString(),
                null,
                new ShipmentCreatedEvent(
                        savedShipment.getId().toString(),
                        savedShipment.getShipmentNumber(),
                        OffsetDateTime.now().toInstant()
                )
        );

        return shipmentMapper.toResponse(savedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getById(UUID id) {
        return shipmentMapper.toResponse(findShipment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getByShipmentNumber(String shipmentNumber) {

        Shipment shipment = shipmentRepository
                .findByShipmentNumber(shipmentNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                ));

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> list(
            ShipmentStatus status,
            UUID customerId,
            Pageable pageable
    ) {
        Page<Shipment> page;

        if (status != null && customerId != null) {
            page = shipmentRepository.findByCustomerIdAndStatus(
                    customerId,
                    status,
                    pageable
            );
        } else if (customerId != null) {
            page = shipmentRepository.findByCustomerId(
                    customerId,
                    pageable
            );
        } else if (status != null) {
            page = shipmentRepository.findByStatus(
                    status,
                    pageable
            );
        } else {
            page = shipmentRepository.findAll(pageable);
        }

        return page.map(shipmentMapper::toResponse);
    }

    @Override
    @Transactional
    public ShipmentResponse confirm(UUID id) {

        Shipment shipment = findShipmentForUpdate(id);

        shipment.changeStatus(ShipmentStatus.CONFIRMED);

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse assign(
            UUID id,
            ShipmentAssignmentRequest request
    ) {
        Shipment shipment = findShipmentForUpdate(id);

        if (shipment.getStatus() != ShipmentStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only confirmed shipments can be assigned"
            );
        }

        var driver = driverRepository.findById(
                request.driverId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Driver not found"
        ));

        var vehicle = vehicleRepository.findById(
                request.vehicleId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Vehicle not found"
        ));

        if (shipmentRepository.countActiveShipmentsForDriver(
                request.driverId(),
                ACTIVE_STATUSES
        ) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Driver already has an active shipment"
            );
        }

        if (shipmentRepository.countActiveShipmentsForVehicle(
                request.vehicleId(),
                ACTIVE_STATUSES
        ) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicle already has an active shipment"
            );
        }

        shipment.assignTo(driver, vehicle);

        // Phase 13: commit the assignment event atomically with the shipment.
        outboxService.record(
                outboxService.shipmentTopic(),
                "ShipmentAssigned",
                "Shipment",
                shipment.getId().toString(),
                null,
                new ShipmentAssignedEvent(
                        shipment.getId().toString(),
                        driver.getId().toString(),
                        vehicle.getId().toString(),
                        OffsetDateTime.now().toInstant()
                )
        );

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateStatus(
            UUID id,
            ShipmentStatusUpdateRequest request
    ) {
        Shipment shipment = findShipmentForUpdate(id);

        ShipmentStatus target = request.targetStatus();

        if (target == ShipmentStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use the dedicated cancellation operation"
            );
        }

        if (target == ShipmentStatus.FAILED) {

            if (request.failureReason() == null
                    || request.failureReason().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Failure reason is required"
                );
            }

            shipment.recordFailure(request.failureReason());

            return shipmentMapper.toResponse(shipment);
        }

        if (target == ShipmentStatus.PICKED_UP) {

            OffsetDateTime pickedUpAt = OffsetDateTime.now();

            shipment.markPickedUp(pickedUpAt);

            // Phase 13: commit the pickup event atomically with the shipment.
            outboxService.record(
                    outboxService.shipmentTopic(),
                    "ShipmentPickedUp",
                    "Shipment",
                    shipment.getId().toString(),
                    null,
                    new ShipmentPickedUpEvent(
                            shipment.getId().toString(),
                            pickedUpAt.toInstant()
                    )
            );

        } else if (target == ShipmentStatus.DELIVERED) {

            OffsetDateTime deliveredAt = OffsetDateTime.now();

            shipment.markDelivered(deliveredAt);

            // Phase 13: commit the delivery event atomically with the shipment.
            outboxService.record(
                    outboxService.shipmentTopic(),
                    "ShipmentDelivered",
                    "Shipment",
                    shipment.getId().toString(),
                    null,
                    new ShipmentDeliveredEvent(
                            shipment.getId().toString(),
                            shipment.getShipmentNumber(),
                            deliveredAt.toInstant()
                    )
            );

        } else {
            shipment.changeStatus(target);
        }

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse cancel(UUID id) {

        Shipment shipment = findShipmentForUpdate(id);

        shipment.cancel();

        return shipmentMapper.toResponse(shipment);
    }

    private Shipment findShipment(UUID id) {

        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                ));
    }

    private Shipment findShipmentForUpdate(UUID id) {

        return shipmentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shipment not found"
                ));
    }

    private String generateShipmentNumber() {

        String date = OffsetDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return "FF-" + date + "-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }


}
