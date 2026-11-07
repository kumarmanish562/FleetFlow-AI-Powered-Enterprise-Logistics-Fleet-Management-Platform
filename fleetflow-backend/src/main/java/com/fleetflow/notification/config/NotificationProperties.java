
package com.fleetflow.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "fleetflow.notifications")
public record NotificationProperties(
        boolean enabled,
        int maxAttempts,
        Duration retryDelay,
        int batchSize,
        String deadLetterTopic
) {
    public NotificationProperties {
        if (maxAttempts < 1 || maxAttempts > 20) {
            throw new IllegalArgumentException(
                    "maxAttempts must be between 1 and 20"
            );
        }

        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException(
                    "batchSize must be between 1 and 1000"
            );
        }

        if (retryDelay == null || retryDelay.isNegative()
                || retryDelay.isZero()) {
            throw new IllegalArgumentException(
                    "retryDelay must be positive"
            );
        }
    }
}
