
package com.fleetflow.notification.provider;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationChannel;

import org.springframework.stereotype.Component;

@Component
public class InAppNotificationProvider implements NotificationProvider {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public NotificationDeliveryResult send(Notification notification) {
        return NotificationDeliveryResult.success(
                notification.getId().toString()
        );
    }
}
