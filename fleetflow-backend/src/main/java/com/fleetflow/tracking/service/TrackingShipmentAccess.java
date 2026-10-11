package com.fleetflow.tracking.service;

import java.util.UUID;

public interface TrackingShipmentAccess {

    void assertCanCreate(UUID shipmentId);

    void assertCanView(UUID shipmentId);
}