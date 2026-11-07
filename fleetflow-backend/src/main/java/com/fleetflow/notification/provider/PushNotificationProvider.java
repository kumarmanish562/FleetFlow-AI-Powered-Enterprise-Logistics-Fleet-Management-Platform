
package com.fleetflow.notification.provider;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationProvider implements NotificationProvider {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public NotificationDeliveryResult send(Notification notification) {
        return NotificationDeliveryResult.permanentFailure(
                "PUSH_PROVIDER_NOT_CONFIGURED",
                "Configure FCM or a web push provider and device subscriptions"
        );
    }
}
