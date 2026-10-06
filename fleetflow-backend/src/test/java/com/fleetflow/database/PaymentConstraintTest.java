package com.fleetflow.database;

import com.fleetflow.payment.entity.Payment;
import com.fleetflow.payment.enums.PaymentMethod;
import com.fleetflow.payment.enums.PaymentStatus;
import com.fleetflow.payment.repository.PaymentRepository;
import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentConstraintTest extends PostgresContainerTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void shouldRejectPaymentWithoutInvoice() {

        Payment payment = Payment.builder()
                .paymentReference("PAY-CONSTRAINT-001")
                .amount(new BigDecimal("1000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.SUCCESS)
                .build();

        assertThatThrownBy(() ->
                paymentRepository.saveAndFlush(payment)
        )
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}