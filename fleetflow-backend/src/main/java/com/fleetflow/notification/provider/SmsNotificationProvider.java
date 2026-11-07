
package com.fleetflow.notification.provider;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationProvider implements NotificationProvider {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public NotificationDeliveryResult send(Notification notification) {
        return NotificationDeliveryResult.permanentFailure(
                "SMS_PROVIDER_NOT_CONFIGURED",
                "Configure an SMS gateway and trusted phone-number resolver"
        );
    }
}
