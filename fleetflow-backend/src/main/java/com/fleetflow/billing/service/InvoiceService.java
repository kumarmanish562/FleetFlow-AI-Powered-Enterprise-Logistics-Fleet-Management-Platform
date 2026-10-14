package com.fleetflow.billing.service;

import com.fleetflow.billing.dto.InvoiceCreateRequest;
import com.fleetflow.billing.dto.InvoiceResponse;
import com.fleetflow.billing.entity.Invoice;
import com.fleetflow.billing.entity.InvoiceItem;
import com.fleetflow.billing.exception.InvoiceNotFoundException;
import com.fleetflow.billing.mapper.InvoiceMapper;
import com.fleetflow.billing.repository.BillingInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class InvoiceService {

    private final BillingInvoiceRepository invoiceRepository;
    private final PricingService pricingService;
    private final ShipmentBillingDataProvider shipmentProvider;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(
            BillingInvoiceRepository invoiceRepository,
            PricingService pricingService,
            ShipmentBillingDataProvider shipmentProvider,
            InvoiceMapper invoiceMapper
    ) {
        this.invoiceRepository = invoiceRepository;
        this.pricingService = pricingService;
        this.shipmentProvider = shipmentProvider;
        this.invoiceMapper = invoiceMapper;
    }

    @Transactional
    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {

        var shipment = shipmentProvider.getBillableShipment(
                request.shipmentId()
        );

        if (!shipment.eligibleForBilling()) {
            throw new IllegalStateException(
                    "Shipment is not eligible for billing"
            );
        }

        LocalDate today = LocalDate.now();

        if (request.dueDate().isBefore(today)) {
            throw new IllegalArgumentException(
                    "Due date cannot be in the past"
            );
        }

        var quote = pricingService.calculate(
                shipment.shipmentId(),
                shipment.distanceKm(),
                shipment.weightKg(),
                shipment.priority()
        );

        String invoiceNumber = "INV-" + today.getYear()
                + "-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();

        // This sample uses policy version 1.
        // Replace with the actual policy version selected for this quote.
        Invoice invoice = new Invoice(
                invoiceNumber,
                shipment.customerId(),
                shipment.shipmentId(),
                quote.currency(),
                quote.subtotal(),
                quote.discountAmount(),
                quote.taxableAmount(),
                quote.taxAmount(),
                quote.totalAmount(),
                request.dueDate(),
                1
        );

        for (var charge : quote.charges()) {
            invoice.addItem(
                    new InvoiceItem(
                            charge.code(),
                            charge.description(),
                            charge.amount(),
                            false
                    )
            );
        }

        if (quote.discountAmount().signum() > 0) {
            invoice.addItem(
                    new InvoiceItem(
                            "DISCOUNT",
                            "Discount applied",
                            quote.discountAmount(),
                            true
                    )
            );
        }

        if (quote.taxAmount().signum() > 0) {
            invoice.addItem(
                    new InvoiceItem(
                            "TAX",
                            "Tax according to selected pricing policy",
                            quote.taxAmount(),
                            false
                    )
            );
        }

        invoice.issue(today);

        return invoiceMapper.toResponse(
                invoiceRepository.saveAndFlush(invoice)
        );
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(invoiceId)
                );

        return invoiceMapper.toResponse(invoice);
    }
}