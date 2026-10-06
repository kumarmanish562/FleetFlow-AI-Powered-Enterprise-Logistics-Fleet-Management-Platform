package com.fleetflow.shipment.repository;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.route.entity.Route;
import com.fleetflow.route.enums.RouteStatus;
import com.fleetflow.route.repository.RouteRepository;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShipmentRepositoryTest
        extends PostgresContainerTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Customer customer;

    private Route route;

    @BeforeEach
    void setUp() {

        customer =
                customerRepository.saveAndFlush(
                        Customer.builder()
                                .name("FleetFlow Customer")
                                .email(
                                        "customer@example.com"
                                )
                                .phone("9876543210")
                                .customerType(
                                        CustomerType.BUSINESS
                                )
                                .status(
                                        CustomerStatus.ACTIVE
                                )
                                .billingAddress(
                                        "Bhilai"
                                )
                                .shippingAddress(
                                        "Raipur"
                                )
                                .build()
                );

        route =
                routeRepository.saveAndFlush(
                        Route.builder()
                                .routeNumber("RT-0001")
                                .originAddress("Bhilai")
                                .destinationAddress(
                                        "Raipur"
                                )
                                .distanceKm(
                                        new BigDecimal("40.50")
                                )
                                .estimatedDurationMinutes(
                                        90
                                )
                                .status(
                                        RouteStatus.PLANNED
                                )
                                .build()
                );
    }

    private Shipment createShipment(
            String shipmentNumber
    ) {

        return Shipment.builder()
                .customer(customer)
                .route(route)
                .shipmentNumber(shipmentNumber)
                .status(
                        ShipmentStatus.CREATED
                )
                .priority(
                        ShipmentPriority.NORMAL
                )
                .originAddress("Bhilai")
                .destinationAddress("Raipur")
                .totalWeightKg(
                        new BigDecimal("50.00")
                )
                .declaredValue(
                        new BigDecimal("10000.00")
                )
                .build();
    }

    @Test
    void shouldSaveShipment() {

        Shipment shipment =
                createShipment("SHP-0001");

        Shipment saved =
                shipmentRepository.saveAndFlush(
                        shipment
                );

        assertThat(saved.getId())
                .isNotNull();

        assertThat(
                saved.getShipmentNumber()
        )
                .isEqualTo("SHP-0001");
    }

    @Test
    void shouldFindShipmentByShipmentNumber() {

        Shipment shipment =
                createShipment("SHP-0002");

        shipmentRepository.saveAndFlush(
                shipment
        );

        var result =
                shipmentRepository
                        .findByShipmentNumber(
                                "SHP-0002"
                        );

        assertThat(result)
                .isPresent();

        assertThat(
                result.get().getShipmentNumber()
        )
                .isEqualTo("SHP-0002");
    }

    @Test
    void shouldCheckShipmentNumberExists() {

        shipmentRepository.saveAndFlush(
                createShipment("SHP-0003")
        );

        boolean exists =
                shipmentRepository
                        .existsByShipmentNumber(
                                "SHP-0003"
                        );

        assertThat(exists)
                .isTrue();
    }

    @Test
    void shouldReturnFalseForUnknownShipmentNumber() {

        boolean exists =
                shipmentRepository
                        .existsByShipmentNumber(
                                "UNKNOWN"
                        );

        assertThat(exists)
                .isFalse();
    }

    @Test
    void shouldFindShipmentsByCustomer() {

        shipmentRepository.saveAndFlush(
                createShipment("SHP-0004")
        );

        Page<Shipment> result =
                shipmentRepository.findByCustomerId(
                        customer.getId(),
                        PageRequest.of(0, 10)
                );

        assertThat(result)
                .isNotEmpty();

        assertThat(result.getContent())
                .anyMatch(
                        shipment ->
                                shipment
                                        .getShipmentNumber()
                                        .equals("SHP-0004")
                );
    }

    @Test
    void shouldFindShipmentsByStatus() {

        shipmentRepository.saveAndFlush(
                createShipment("SHP-0005")
        );

        Page<Shipment> result =
                shipmentRepository.findByStatus(
                        ShipmentStatus.CREATED,
                        PageRequest.of(0, 10)
                );

        assertThat(result)
                .isNotEmpty();

        assertThat(result.getContent())
                .anyMatch(
                        shipment ->
                                shipment
                                        .getShipmentNumber()
                                        .equals("SHP-0005")
                );
    }

    @Test
    void shouldSupportPagination() {

        shipmentRepository.saveAndFlush(
                createShipment("SHP-0006")
        );

        shipmentRepository.saveAndFlush(
                createShipment("SHP-0007")
        );

        shipmentRepository.saveAndFlush(
                createShipment("SHP-0008")
        );

        Page<Shipment> page =
                shipmentRepository.findByCustomerId(
                        customer.getId(),
                        PageRequest.of(0, 2)
                );

        assertThat(page.getSize())
                .isEqualTo(2);

        assertThat(page.getTotalElements())
                .isGreaterThanOrEqualTo(3);
    }
}