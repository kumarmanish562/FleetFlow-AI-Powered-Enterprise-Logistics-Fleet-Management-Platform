
package com.fleetflow.notification.provider;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationChannel;

public interface NotificationProvider {

    NotificationChannel channel();

    NotificationDeliveryResult send(Notification notification);
}
