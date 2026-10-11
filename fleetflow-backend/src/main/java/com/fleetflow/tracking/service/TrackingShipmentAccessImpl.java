
package com.fleetflow.tracking.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TrackingShipmentAccessImpl
        implements TrackingShipmentAccess {

    @Override
    public void assertCanCreate(UUID shipmentId) {
        /*
         * TODO: Implement using FleetFlow's actual security model.
         *
         * Verify that the authenticated driver is assigned to this
         * shipment, or that the authenticated user has an explicitly
         * permitted administrative role.
         *
         * Until this check is implemented, reject the request.
         */
        throw new UnsupportedOperationException(
                "Shipment tracking authorization is not configured"
        );
    }

    @Override
    public void assertCanView(UUID shipmentId) {
        /*
         * TODO: Implement using FleetFlow's actual security model.
         *
         * Verify shipment access for the authenticated principal.
         * Apply the same fail-closed policy as assertCanCreate().
         */
        throw new UnsupportedOperationException(
                "Shipment tracking authorization is not configured"
        );
    }
}
