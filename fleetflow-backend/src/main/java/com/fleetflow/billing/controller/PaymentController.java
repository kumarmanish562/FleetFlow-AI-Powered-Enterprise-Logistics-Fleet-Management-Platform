package com.fleetflow.billing.controller;

import com.fleetflow.billing.dto.PaymentCreateRequest;
import com.fleetflow.billing.dto.PaymentResponse;
import com.fleetflow.billing.service.AuthenticatedCustomerProvider;
import com.fleetflow.billing.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices/{invoiceId}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthenticatedCustomerProvider customerProvider;

    public PaymentController(
            PaymentService paymentService,
            AuthenticatedCustomerProvider customerProvider
    ) {
        this.paymentService = paymentService;
        this.customerProvider = customerProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse initiatePayment(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        if (!invoiceId.equals(request.invoiceId())) {
            throw new IllegalArgumentException(
                    "Path invoice ID and request invoice ID do not match"
            );
        }

        Long customerId = customerProvider.requireCurrentCustomerId();

        return paymentService.initiatePayment(customerId, request);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse getPayment(
            @PathVariable UUID invoiceId,
            @PathVariable UUID paymentId
    ) {
        Long customerId = customerProvider.requireCurrentCustomerId();

        PaymentResponse payment = paymentService.getPayment(
                paymentId,
                customerId
        );

        if (!invoiceId.equals(payment.invoiceId())) {
            throw new IllegalArgumentException(
                    "Payment does not belong to the specified invoice"
            );
        }

        return payment;
    }
}