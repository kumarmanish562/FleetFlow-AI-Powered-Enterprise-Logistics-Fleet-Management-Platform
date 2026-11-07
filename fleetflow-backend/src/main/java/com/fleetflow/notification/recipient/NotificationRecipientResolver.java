
package com.fleetflow.notification.recipient;

import java.util.UUID;

public interface NotificationRecipientResolver {

    NotificationRecipient resolveCustomer(UUID customerId);

    NotificationRecipient resolveShipmentCustomer(UUID shipmentId);

    NotificationRecipient resolveInvoiceCustomer(UUID invoiceId);

    NotificationRecipient resolvePaymentCustomer(UUID paymentId);
}
