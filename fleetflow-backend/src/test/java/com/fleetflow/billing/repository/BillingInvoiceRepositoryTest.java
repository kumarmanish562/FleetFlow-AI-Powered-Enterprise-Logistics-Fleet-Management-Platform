package com.fleetflow.billing.repository;

import com.fleetflow.billing.entity.Invoice;
import com.fleetflow.billing.enums.InvoiceStatus;
import com.fleetflow.support.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BillingInvoiceRepositoryTest extends PostgresContainerTest {

    @Autowired
    private BillingInvoiceRepository invoiceRepository;

    @Test
    void shouldSaveAndFindInvoice() {
        Invoice invoice = new Invoice(
                "INV-" + java.util.UUID.randomUUID(),
                101L,
                201L,
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

        var result = invoiceRepository.findById(invoice.getId());
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(result.orElseThrow().getTotalAmount()).isEqualByComparingTo("1180.00");
    }
}
