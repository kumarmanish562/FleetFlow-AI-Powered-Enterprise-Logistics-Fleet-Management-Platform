package com.fleetflow.integration.routing;

public interface RoutingService {

    RouteEstimate calculateRoute(
            GeoLocation origin,
            GeoLocation destination
    );
}