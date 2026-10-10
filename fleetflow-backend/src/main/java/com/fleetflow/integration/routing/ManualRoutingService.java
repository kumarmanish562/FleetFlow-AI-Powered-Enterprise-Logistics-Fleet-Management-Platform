package com.fleetflow.integration.routing;

import org.springframework.stereotype.Component;

@Component
public class ManualRoutingService implements RoutingService {

    @Override
    public RouteEstimate calculateRoute(
            GeoLocation origin,
            GeoLocation destination) {

        throw new UnsupportedOperationException(
                "Manual routing does not calculate road distance. "
                        + "Use an authorized, verified manual estimate or "
                        + "configure a real routing provider."
        );
    }
}