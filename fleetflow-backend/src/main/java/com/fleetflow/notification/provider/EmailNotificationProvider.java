
package com.fleetflow.notification.provider;

import com.fleetflow.notification.entity.Notification;
import com.fleetflow.notification.enums.NotificationChannel;
import com.fleetflow.notification.exception.NotificationDeliveryException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider implements NotificationProvider {

    private final JavaMailSender mailSender;

    @Value("${fleetflow.notifications.email.from}")
    private String from;

    public EmailNotificationProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public NotificationDeliveryResult send(Notification notification) {
        // Resolve the destination from trusted user/contact data.
        // Never use a client-supplied email address as authorization.
        throw new NotificationDeliveryException(
                "Configure a trusted email recipient resolver before enabling SMTP",
                false
        );
    }

    public void sendTo(String recipient, Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject(notification.getTitle());
        message.setText(notification.getMessage());
        mailSender.send(message);
    }
}
