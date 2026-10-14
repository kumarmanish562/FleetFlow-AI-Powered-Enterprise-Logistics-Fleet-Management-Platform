package com.fleetflow.billing.repository;

import com.fleetflow.billing.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BillingInvoiceRepository
        extends JpaRepository<Invoice, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :invoiceId")
    Optional<Invoice> findByIdForUpdate(
            @Param("invoiceId") UUID invoiceId
    );
}