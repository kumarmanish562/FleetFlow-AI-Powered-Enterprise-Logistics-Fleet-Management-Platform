package com.fleetflow.payment.repository;

import com.fleetflow.invoice.entity.Invoice;
import com.fleetflow.payment.entity.Payment;
import com.fleetflow.payment.enums.PaymentMethod;
import com.fleetflow.payment.enums.PaymentStatus;
import com.fleetflow.support.PostgresContainerTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest extends PostgresContainerTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void shouldSavePayment() {

        /*
         * Create:
         *
         * Customer
         *      ↓
         * Shipment
         *      ↓
         * Invoice
         *      ↓
         * Payment
         *
         * Use exactly the same Customer → Shipment → Invoice
         * setup from your InvoiceRepositoryTest.
         */

        Invoice invoice = createInvoice();

        Payment payment =
                Payment.builder()
                        .invoice(invoice)
                        .paymentReference("PAY-0001")
                        .amount(new BigDecimal("1180"))
                        .paymentMethod(PaymentMethod.UPI)
                        .status(PaymentStatus.SUCCESS)
                        .build();

        Payment saved =
                paymentRepository.saveAndFlush(payment);

        assertThat(saved.getId())
                .isNotNull();

        assertThat(saved.getPaymentReference())
                .isEqualTo("PAY-0001");

        assertThat(saved.getAmount())
                .isEqualByComparingTo("1180");

        assertThat(saved.getInvoice())
                .isNotNull();

        assertThat(saved.getInvoice().getId())
                .isEqualTo(invoice.getId());
    }

    @Test
    void shouldFindPaymentsByInvoice() {

        Invoice invoice = createInvoice();

        Payment payment =
                Payment.builder()
                        .invoice(invoice)
                        .paymentReference("PAY-0002")
                        .amount(new BigDecimal("1180"))
                        .paymentMethod(PaymentMethod.UPI)
                        .status(PaymentStatus.SUCCESS)
                        .build();

        paymentRepository.saveAndFlush(payment);

        var result =
                paymentRepository.findByInvoiceId(
                        invoice.getId()
                );

        assertThat(result)
                .hasSize(1);

        assertThat(
                result.get(0).getPaymentReference()
        )
                .isEqualTo("PAY-0002");
    }

    private Invoice createInvoice() {

        /*
         * Put the exact Customer → Shipment → Invoice
         * creation code from InvoiceRepositoryTest here.
         */

        return null;
    }
}