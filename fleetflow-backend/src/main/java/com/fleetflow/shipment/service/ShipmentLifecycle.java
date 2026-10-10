package com.fleetflow.shipment.service;

import com.fleetflow.shipment.enums.ShipmentStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class ShipmentLifecycle {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> TRANSITIONS =
            new EnumMap<>(ShipmentStatus.class);

    static {
        TRANSITIONS.put(ShipmentStatus.CREATED,
                EnumSet.of(ShipmentStatus.CONFIRMED, ShipmentStatus.CANCELLED));
        TRANSITIONS.put(ShipmentStatus.CONFIRMED,
                EnumSet.of(ShipmentStatus.PICKUP_SCHEDULED, ShipmentStatus.CANCELLED));
        TRANSITIONS.put(ShipmentStatus.PICKUP_SCHEDULED,
                EnumSet.of(ShipmentStatus.PICKED_UP, ShipmentStatus.CANCELLED,
                        ShipmentStatus.FAILED));
        TRANSITIONS.put(ShipmentStatus.PICKED_UP,
                EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.FAILED));
        TRANSITIONS.put(ShipmentStatus.IN_TRANSIT,
                EnumSet.of(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.FAILED));
        TRANSITIONS.put(ShipmentStatus.OUT_FOR_DELIVERY,
                EnumSet.of(ShipmentStatus.DELIVERED, ShipmentStatus.FAILED));
        TRANSITIONS.put(ShipmentStatus.FAILED, EnumSet.of(ShipmentStatus.RETURNED));
        TRANSITIONS.put(ShipmentStatus.DELIVERED, EnumSet.noneOf(ShipmentStatus.class));
        TRANSITIONS.put(ShipmentStatus.CANCELLED, EnumSet.noneOf(ShipmentStatus.class));
        TRANSITIONS.put(ShipmentStatus.RETURNED, EnumSet.noneOf(ShipmentStatus.class));
    }

    private ShipmentLifecycle() {}

    public static boolean canTransition(ShipmentStatus current, ShipmentStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return TRANSITIONS.getOrDefault(current, EnumSet.noneOf(ShipmentStatus.class))
                .contains(target);
    }

    public static void validateTransition(ShipmentStatus current, ShipmentStatus target) {
        if (!canTransition(current, target)) {
            throw new IllegalStateException(
                    "Invalid shipment status transition: " + current + " -> " + target);
        }
    }
}
