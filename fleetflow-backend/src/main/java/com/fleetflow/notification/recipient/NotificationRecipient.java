
package com.fleetflow.notification.recipient;

import java.util.UUID;

public record NotificationRecipient(
        UUID userId,
        String email,
        String phoneNumber
) {
}
