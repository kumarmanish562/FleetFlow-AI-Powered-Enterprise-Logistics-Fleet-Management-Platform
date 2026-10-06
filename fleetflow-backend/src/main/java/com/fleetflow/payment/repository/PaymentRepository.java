package com.fleetflow.payment.repository;

import com.fleetflow.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentReference(
            String paymentReference
    );

    List<Payment> findByInvoiceId(
            UUID invoiceId
    );
}