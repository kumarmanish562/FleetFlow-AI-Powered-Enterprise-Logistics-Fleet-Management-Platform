package com.fleetflow.route.service;

import com.fleetflow.route.dto.CreateRouteRequest;
import com.fleetflow.route.dto.RouteResponse;
import com.fleetflow.route.entity.Route;
import com.fleetflow.route.enums.RouteStatus;
import com.fleetflow.route.mapper.RouteMapper;
import com.fleetflow.route.repository.RouteRepository;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.repository.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteMapper routeMapper;

    @Transactional
    public RouteResponse createRoute(CreateRouteRequest request) {

        Shipment shipment = shipmentRepository
                .findById(request.shipmentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipment not found: " + request.shipmentId()
                ));

        // Validate the actual shipment lifecycle.
        if (shipment.getStatus() != ShipmentStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only confirmed shipments can be assigned to a route"
            );
        }

        // Prevent assigning the shipment to another route.
        if (shipment.getRoute() != null) {
            throw new IllegalStateException(
                    "Shipment is already assigned to a route"
            );
        }

        // Read the actual address fields from Shipment.java.
        String origin = shipment.getOriginAddress();
        String destination = shipment.getDestinationAddress();

        if (origin == null || origin.isBlank()
                || destination == null || destination.isBlank()) {
            throw new IllegalStateException(
                    "Shipment origin and destination addresses are required"
            );
        }

        Route route = Route.builder()
                .routeNumber(generateRouteNumber())
                .originAddress(origin.trim())
                .destinationAddress(destination.trim())
                .plannedDepartureAt(request.plannedDepartureAt())
                .status(RouteStatus.PLANNED)
                .build();

        Route savedRoute = routeRepository.save(route);

        // Maintain the existing Shipment.route_id relationship.
        shipment.assignRoute(savedRoute);
        shipmentRepository.save(shipment);

        return routeMapper.toResponse(savedRoute);
    }

    public RouteResponse getRoute(UUID routeId) {

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Route not found: " + routeId
                ));

        return routeMapper.toResponse(route);
    }

    public Page<RouteResponse> getRoutes(
            RouteStatus status,
            Pageable pageable) {

        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException(
                    "Page size cannot exceed 100"
            );
        }

        Page<Route> routes = status == null
                ? routeRepository.findAll(pageable)
                : routeRepository.findByStatus(status, pageable);

        return routes.map(routeMapper::toResponse);
    }

    private String generateRouteNumber() {
        return "RTE-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}