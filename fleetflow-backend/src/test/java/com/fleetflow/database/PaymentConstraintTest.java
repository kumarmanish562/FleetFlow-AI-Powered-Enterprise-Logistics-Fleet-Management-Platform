package com.fleetflow.database;

import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentConstraintTest extends PostgresContainerTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldRejectPaymentWithoutInvoice() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO billing_payments
                    (id, invoice_id, customer_id, amount, currency, method, status,
                     provider, idempotency_key, created_at, version)
                VALUES (?, NULL, 1, ?, 'INR', 'UPI', 'PENDING', 'test', 'constraint-test', now(), 0)
                """, UUID.randomUUID(), new BigDecimal("1000.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
