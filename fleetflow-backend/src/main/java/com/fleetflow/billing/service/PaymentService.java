package com.fleetflow.billing.service;

import com.fleetflow.billing.dto.PaymentCreateRequest;
import com.fleetflow.billing.dto.PaymentResponse;
import com.fleetflow.billing.entity.Payment;
import com.fleetflow.billing.exception.InvoiceNotFoundException;
import com.fleetflow.billing.exception.PaymentException;
import com.fleetflow.billing.provider.PaymentProvider;
import com.fleetflow.billing.repository.BillingInvoiceRepository;
import com.fleetflow.billing.repository.BillingPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final BillingInvoiceRepository invoiceRepository;
    private final BillingPaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;

    public PaymentService(
            BillingInvoiceRepository invoiceRepository,
            BillingPaymentRepository paymentRepository,
            PaymentProvider paymentProvider
    ) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
    }

    @Transactional
    public PaymentResponse initiatePayment(
            Long authenticatedCustomerId,
            PaymentCreateRequest request
    ) {
        var existing = paymentRepository
                .findByCustomerIdAndIdempotencyKey(
                        authenticatedCustomerId,
                        request.idempotencyKey()
                );

        if (existing.isPresent()) {
            Payment previous = existing.get();

            if (!previous.getInvoice().getId().equals(request.invoiceId())
                    || previous.getMethod() != request.method()) {
                throw new PaymentException(
                        "Idempotency key was reused with different payment details"
                );
            }

            return toResponse(previous);
        }

        var invoice = invoiceRepository
                .findByIdForUpdate(request.invoiceId())
                .orElseThrow(() ->
                        new InvoiceNotFoundException(request.invoiceId())
                );

        if (!invoice.getCustomerId().equals(authenticatedCustomerId)) {
            throw new PaymentException("Invoice access denied");
        }

        if (invoice.getOutstandingBalance().signum() <= 0) {
            throw new PaymentException(
                    "Invoice has no outstanding balance"
            );
        }

        Payment payment = new Payment(
                invoice,
                authenticatedCustomerId,
                invoice.getOutstandingBalance(),
                request.method(),
                paymentProvider.providerName(),
                request.idempotencyKey()
        );

        Payment saved = paymentRepository.saveAndFlush(payment);

        try {
            var result = paymentProvider.initiatePayment(
                    saved.getIdempotencyKey(),
                    saved.getAmount(),
                    saved.getCurrency(),
                    saved.getMethod().name()
            );

            saved.recordProviderReference(result.providerReference());

            return toResponse(saved);

        } catch (RuntimeException exception) {
            // Do not blindly mark a payment failed after a timeout:
            // the provider may have processed the request.
            // A production implementation must recover using the
            // provider's idempotency key and transaction lookup API.
            throw new PaymentException(
                    "Payment initiation requires provider reconciliation",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            UUID paymentId,
            Long authenticatedCustomerId
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentException("Payment not found")
                );

        if (!payment.getCustomerId().equals(authenticatedCustomerId)) {
            throw new PaymentException("Payment access denied");
        }

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getProvider(),
                payment.getProviderReference(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}