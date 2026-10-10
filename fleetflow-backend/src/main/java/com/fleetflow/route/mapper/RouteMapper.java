package com.fleetflow.route.mapper;

import com.fleetflow.route.dto.RouteResponse;
import com.fleetflow.route.entity.Route;
import org.springframework.stereotype.Component;

@Component
public class RouteMapper {

    public RouteResponse toResponse(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getRouteNumber(),
                route.getOriginAddress(),
                route.getDestinationAddress(),
                route.getDistanceKm(),
                route.getEstimatedDurationMinutes(),
                route.getPlannedDepartureAt(),
                route.getEstimatedArrivalAt(),
                route.getStatus()
        );
    }
}