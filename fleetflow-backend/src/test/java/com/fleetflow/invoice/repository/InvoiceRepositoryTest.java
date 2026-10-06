package com.fleetflow.invoice.repository;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.invoice.entity.Invoice;
import com.fleetflow.invoice.enums.InvoiceStatus;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;


import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InvoiceRepositoryTest
        extends PostgresContainerTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldSaveAndFindInvoice() {

        Customer customer =
                customerRepository.saveAndFlush(
                        Customer.builder()
                                .name("Invoice Customer")
                                .customerType(
                                        CustomerType.BUSINESS
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
                                        "SHP-INV-001"
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

        Invoice invoice =
                Invoice.builder()
                        .shipment(shipment)
                        .invoiceNumber("INV-0001")
                        .subtotal(
                                new BigDecimal("1000")
                        )
                        .taxAmount(
                                new BigDecimal("180")
                        )
                        .discountAmount(
                                BigDecimal.ZERO
                        )
                        .totalAmount(
                                new BigDecimal("1180")
                        )
                        .currency("INR")
                        .status(
                                InvoiceStatus.ISSUED
                        )
                        .build();

        invoiceRepository.saveAndFlush(invoice);

        var result =
                invoiceRepository
                        .findByInvoiceNumber(
                                "INV-0001"
                        );

        assertThat(result)
                .isPresent();

        assertThat(
                result.get().getTotalAmount()
        )
                .isEqualByComparingTo(
                        new BigDecimal("1180")
                );
    }
}