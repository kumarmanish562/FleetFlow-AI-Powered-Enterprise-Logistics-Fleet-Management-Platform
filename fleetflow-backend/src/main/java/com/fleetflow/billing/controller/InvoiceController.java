package com.fleetflow.billing.controller;

import com.fleetflow.billing.dto.InvoiceCreateRequest;
import com.fleetflow.billing.dto.InvoiceResponse;
import com.fleetflow.billing.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING')")
    public InvoiceResponse createInvoice(
            @Valid @RequestBody InvoiceCreateRequest request
    ) {
        return invoiceService.createInvoice(request);
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING', 'CUSTOMER')")
    public InvoiceResponse getInvoice(
            @PathVariable UUID invoiceId
    ) {
        // Before production, use a service method that checks
        // the authenticated customer's ownership of this invoice.
        return invoiceService.getInvoice(invoiceId);
    }
}