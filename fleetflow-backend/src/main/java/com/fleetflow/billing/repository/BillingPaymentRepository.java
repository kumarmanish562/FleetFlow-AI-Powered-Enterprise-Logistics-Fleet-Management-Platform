package com.fleetflow.billing.repository;

import com.fleetflow.billing.entity.Payment;
import com.fleetflow.billing.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingPaymentRepository
        extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByInvoice_IdOrderByCreatedAtDesc(UUID invoiceId);

    Optional<Payment> findByCustomerIdAndIdempotencyKey(
            Long customerId,
            String idempotencyKey
    );

    Optional<Payment> findByProviderAndProviderReference(
            String provider,
            String providerReference
    );

    boolean existsByInvoice_IdAndStatus(
            UUID invoiceId,
            PaymentStatus status
    );
}