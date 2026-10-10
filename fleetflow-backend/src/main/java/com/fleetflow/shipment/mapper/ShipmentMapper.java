package com.fleetflow.shipment.mapper;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.shipment.dto.ShipmentCreateRequest;
import com.fleetflow.shipment.dto.ShipmentResponse;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentStatus;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public Shipment toEntity(ShipmentCreateRequest request, Customer customer,
                             String shipmentNumber) {
        return Shipment.builder()
                .customer(customer)
                .shipmentNumber(shipmentNumber)
                .status(ShipmentStatus.CREATED)
                .priority(request.priority())
                .originAddress(request.originAddress().trim())
                .destinationAddress(request.destinationAddress().trim())
                .totalWeightKg(request.totalWeightKg())
                .declaredValue(request.declaredValue())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .build();
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getCustomer().getId(),
                shipment.getDriver() == null ? null : shipment.getDriver().getId(),
                shipment.getVehicle() == null ? null : shipment.getVehicle().getId(),
                shipment.getRoute() == null ? null : shipment.getRoute().getId(),
                shipment.getStatus(),
                shipment.getPriority(),
                shipment.getOriginAddress(),
                shipment.getDestinationAddress(),
                shipment.getTotalWeightKg(),
                shipment.getDeclaredValue(),
                shipment.getPickupDate(),
                shipment.getExpectedDeliveryDate(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }
}
