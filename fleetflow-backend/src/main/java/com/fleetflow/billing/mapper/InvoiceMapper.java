package com.fleetflow.billing.mapper;

import com.fleetflow.billing.dto.InvoiceItemResponse;
import com.fleetflow.billing.dto.InvoiceResponse;
import com.fleetflow.billing.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {

        var items = invoice.getItems().stream()
                .map(item -> new InvoiceItemResponse(
                        item.getId(),
                        item.getCode(),
                        item.getDescription(),
                        item.getAmount(),
                        item.isDeduction()
                ))
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getCustomerId(),
                invoice.getShipmentId(),
                invoice.getCurrency(),
                invoice.getSubtotal(),
                invoice.getDiscountAmount(),
                invoice.getTaxableAmount(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getAmountPaid(),
                invoice.getOutstandingBalance(),
                invoice.getStatus(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getCreatedAt(),
                items
        );
    }
}