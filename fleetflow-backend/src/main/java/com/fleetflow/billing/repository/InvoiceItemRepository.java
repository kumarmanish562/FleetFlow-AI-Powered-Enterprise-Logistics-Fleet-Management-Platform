package com.fleetflow.billing.repository;

import com.fleetflow.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, UUID> {
}