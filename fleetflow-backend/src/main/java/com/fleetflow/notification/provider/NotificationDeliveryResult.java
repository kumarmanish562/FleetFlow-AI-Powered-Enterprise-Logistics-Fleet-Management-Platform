
package com.fleetflow.notification.provider;

public record NotificationDeliveryResult(
        boolean successful,
        boolean retryable,
        String providerMessageId,
        String errorCode,
        String errorMessage
) {
    public static NotificationDeliveryResult success(String messageId) {
        return new NotificationDeliveryResult(
                true, false, messageId, null, null
        );
    }

    public static NotificationDeliveryResult retryableFailure(
            String code,
            String message
    ) {
        return new NotificationDeliveryResult(
                false, true, null, code, message
        );
    }

    public static NotificationDeliveryResult permanentFailure(
            String code,
            String message
    ) {
        return new NotificationDeliveryResult(
                false, false, null, code, message
        );
    }
}
