package com.fleetflow.billing.repository;

import com.fleetflow.billing.entity.Invoice;
import com.fleetflow.billing.entity.Payment;
import com.fleetflow.billing.enums.PaymentMethod;
import com.fleetflow.billing.enums.PaymentStatus;
import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BillingPaymentRepositoryTest extends PostgresContainerTest {

    @Autowired
    private BillingPaymentRepository paymentRepository;

    @Autowired
    private BillingInvoiceRepository invoiceRepository;

    private Invoice invoice;
    private final Long customerId = 501L;

    @BeforeEach
    void setUp() {
        invoice = new Invoice(
                "INV-PAY-" + java.util.UUID.randomUUID(),
                customerId,
                601L,
                "INR",
                new BigDecimal("1000.00"),
                BigDecimal.ZERO.setScale(2),
                new BigDecimal("1000.00"),
                new BigDecimal("180.00"),
                new BigDecimal("1180.00"),
                LocalDate.now().plusDays(30),
                1);
        invoice.issue(LocalDate.now());
        invoice = invoiceRepository.saveAndFlush(invoice);
    }

    @Test
    void shouldSavePayment() {
        Payment payment = createPayment(new BigDecimal("1180.00"), PaymentMethod.UPI);
        payment.markSuccessful("provider-" + java.util.UUID.randomUUID(), Instant.now());
        Payment saved = paymentRepository.saveAndFlush(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("1180.00");
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.UPI);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(saved.getInvoice().getId()).isEqualTo(invoice.getId());
    }

    @Test
    void shouldFindPaymentsByInvoice() {
        Payment firstPayment = createPayment(new BigDecimal("500.00"), PaymentMethod.UPI);
        Payment secondPayment = createPayment(new BigDecimal("680.00"), PaymentMethod.BANK_TRANSFER);
        paymentRepository.saveAndFlush(firstPayment);
        paymentRepository.saveAndFlush(secondPayment);

        List<Payment> payments = paymentRepository.findAllByInvoice_IdOrderByCreatedAtDesc(invoice.getId());
        assertThat(payments).hasSize(2)
                .allSatisfy(payment -> assertThat(payment.getInvoice().getId()).isEqualTo(invoice.getId()));
    }

    @Test
    void shouldFindPaymentByProviderReference() {
        String reference = "provider-" + java.util.UUID.randomUUID();
        Payment payment = createPayment(new BigDecimal("750.00"), PaymentMethod.BANK_TRANSFER);
        payment.markSuccessful(reference, Instant.now());
        paymentRepository.saveAndFlush(payment);

        var result = paymentRepository.findByProviderAndProviderReference("test-provider", reference);
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getAmount()).isEqualByComparingTo("750.00");
    }

    private Payment createPayment(BigDecimal amount, PaymentMethod method) {
        return new Payment(invoice, customerId, amount, method, "test-provider",
                java.util.UUID.randomUUID().toString());
    }
}
