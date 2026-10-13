
package com.fleetflow.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetflow.messaging")
public record MessagingProperties(
        Topics topics,
        Outbox outbox
) {
    public record Topics(
            String shipment,
            String vehicle,
            String driver,
            String notificationDlt
    ) {}

    public record Outbox(
            long pollDelayMs,
            int batchSize,
            int maxAttempts
    ) {}
}
