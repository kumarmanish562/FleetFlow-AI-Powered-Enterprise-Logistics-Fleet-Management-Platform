package com.fleetflow.payment.repository;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.invoice.entity.Invoice;
import com.fleetflow.invoice.enums.InvoiceStatus;
import com.fleetflow.invoice.repository.InvoiceRepository;
import com.fleetflow.payment.entity.Payment;
import com.fleetflow.payment.enums.PaymentMethod;
import com.fleetflow.payment.enums.PaymentStatus;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentRepositoryTest extends PostgresContainerTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Invoice invoice;

    @BeforeEach
    void setUp() {

        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        shipmentRepository.deleteAll();
        customerRepository.deleteAll();

        // ---------------------------------------------------------
        // Create Customer
        // ---------------------------------------------------------
        Customer customer = Customer.builder()
                .name("Payment Test Customer")
                .email("payment-test@fleetflow.com")
                .phone("9876543210")
                .customerType(CustomerType.BUSINESS)
                .status(CustomerStatus.ACTIVE)
                .billingAddress("Bhilai, Chhattisgarh")
                .shippingAddress("Raipur, Chhattisgarh")
                .build();

        customer = customerRepository.saveAndFlush(customer);

        // ---------------------------------------------------------
        // Create Shipment
        // ---------------------------------------------------------
        Shipment shipment = Shipment.builder()
                .customer(customer)
                .shipmentNumber("PAY-TEST-SHP-001")
                .status(ShipmentStatus.DELIVERED)
                .priority(ShipmentPriority.NORMAL)
                .originAddress("Bhilai")
                .destinationAddress("Raipur")
                .build();

        shipment = shipmentRepository.saveAndFlush(shipment);

        // ---------------------------------------------------------
        // Create Invoice
        // ---------------------------------------------------------
        invoice = Invoice.builder()
                .shipment(shipment)
                .invoiceNumber("INV-PAY-TEST-001")
                .subtotal(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("180.00"))
                .discountAmount(new BigDecimal("0.00"))
                .totalAmount(new BigDecimal("1180.00"))
                .currency("INR")
                .status(InvoiceStatus.ISSUED)
                .issuedAt(OffsetDateTime.now())
                .dueAt(OffsetDateTime.now().plusDays(30))
                .build();

        invoice = invoiceRepository.saveAndFlush(invoice);
    }

    @Test
    void shouldSavePayment() {

        Payment payment = Payment.builder()
                .invoice(invoice)
                .paymentReference("PAY-0001")
                .amount(new BigDecimal("1180.00"))
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.SUCCESS)
                .paidAt(OffsetDateTime.now())
                .build();

        Payment saved = paymentRepository.saveAndFlush(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPaymentReference())
                .isEqualTo("PAY-0001");
        assertThat(saved.getAmount())
                .isEqualByComparingTo("1180.00");
        assertThat(saved.getPaymentMethod())
                .isEqualTo(PaymentMethod.UPI);
        assertThat(saved.getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);
        assertThat(saved.getInvoice())
                .isEqualTo(invoice);
    }

    @Test
    void shouldFindPaymentsByInvoice() {

        Payment firstPayment = Payment.builder()
                .invoice(invoice)
                .paymentReference("PAY-0001")
                .amount(new BigDecimal("500.00"))
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.SUCCESS)
                .paidAt(OffsetDateTime.now().minusDays(1))
                .build();

        Payment secondPayment = Payment.builder()
                .invoice(invoice)
                .paymentReference("PAY-0002")
                .amount(new BigDecimal("680.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.SUCCESS)
                .paidAt(OffsetDateTime.now())
                .build();

        paymentRepository.saveAndFlush(firstPayment);
        paymentRepository.saveAndFlush(secondPayment);

        List<Payment> payments =
                paymentRepository.findByInvoiceId(invoice.getId());

        assertThat(payments)
                .hasSize(2);

        assertThat(payments)
                .extracting(Payment::getPaymentReference)
                .containsExactlyInAnyOrder(
                        "PAY-0001",
                        "PAY-0002"
                );
    }

    @Test
    void shouldFindPaymentByReference() {

        Payment payment = Payment.builder()
                .invoice(invoice)
                .paymentReference("PAY-REFERENCE-001")
                .amount(new BigDecimal("750.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.SUCCESS)
                .paidAt(OffsetDateTime.now())
                .build();

        paymentRepository.saveAndFlush(payment);

        Optional<Payment> result =
                paymentRepository.findByPaymentReference(
                        "PAY-REFERENCE-001"
                );

        assertThat(result)
                .isPresent();

        assertThat(result.get().getPaymentReference())
                .isEqualTo("PAY-REFERENCE-001");

        assertThat(result.get().getAmount())
                .isEqualByComparingTo("750.00");
    }
}