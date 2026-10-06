package com.fleetflow.database;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.entity.ShipmentItem;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.repository.ShipmentItemRepository;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ShipmentItemConstraintTest
        extends PostgresContainerTest {

    @Autowired
    private ShipmentItemRepository shipmentItemRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldRejectZeroQuantity() {

        Customer customer =
                customerRepository.saveAndFlush(
                        Customer.builder()
                                .name("Customer")
                                .customerType(
                                        CustomerType.INDIVIDUAL
                                )
                                .status(
                                        CustomerStatus.ACTIVE
                                )
                                .build()
                );

        Shipment shipment =
                shipmentRepository.saveAndFlush(
                        Shipment.builder()
                                .customer(customer)
                                .shipmentNumber(
                                        "SHP-QTY-001"
                                )
                                .status(
                                        ShipmentStatus.CREATED
                                )
                                .priority(
                                        ShipmentPriority.NORMAL
                                )
                                .originAddress("Bhilai")
                                .destinationAddress("Raipur")
                                .build()
                );

        ShipmentItem item =
                ShipmentItem.builder()
                        .shipment(shipment)
                        .description("Laptop")
                        .quantity(0)
                        .build();

        assertThatThrownBy(() ->
                shipmentItemRepository
                        .saveAndFlush(item)
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        Customer customer =
                customerRepository.saveAndFlush(
                        Customer.builder()
                                .name("Customer")
                                .customerType(
                                        CustomerType.INDIVIDUAL
                                )
                                .status(
                                        CustomerStatus.ACTIVE
                                )
                                .build()
                );

        Shipment shipment =
                shipmentRepository.saveAndFlush(
                        Shipment.builder()
                                .customer(customer)
                                .shipmentNumber(
                                        "SHP-QTY-002"
                                )
                                .status(
                                        ShipmentStatus.CREATED
                                )
                                .priority(
                                        ShipmentPriority.NORMAL
                                )
                                .originAddress("Bhilai")
                                .destinationAddress("Raipur")
                                .build()
                );

        ShipmentItem item =
                ShipmentItem.builder()
                        .shipment(shipment)
                        .description("Laptop")
                        .quantity(-1)
                        .build();

        assertThatThrownBy(() ->
                shipmentItemRepository
                        .saveAndFlush(item)
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }
}