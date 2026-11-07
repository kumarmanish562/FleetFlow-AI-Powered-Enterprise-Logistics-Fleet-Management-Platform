
package com.fleetflow.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetflow.messaging")
public record MessagingProperties(
        Topics topics,
        Outbox outbox
) {
    private static final Topics DEFAULT_TOPICS = new Topics(
            "fleetflow.shipment.events.v1",
            "fleetflow.vehicle.events.v1",
            "fleetflow.driver.events.v1",
            "fleetflow.shipment.notifications.dlt.v1"
    );

    public MessagingProperties {
        topics = topics == null ? DEFAULT_TOPICS : topics.withDefaults();
        outbox = outbox == null ? new Outbox(1000, 50, 10) : outbox.withDefaults();
    }

    public record Topics(
            String shipmentEvents,
            String vehicleEvents,
            String driverEvents,
            String shipmentNotificationDlt
    ) {
        private Topics withDefaults() {
            return new Topics(
                    valueOr(shipmentEvents, DEFAULT_TOPICS.shipmentEvents),
                    valueOr(vehicleEvents, DEFAULT_TOPICS.vehicleEvents),
                    valueOr(driverEvents, DEFAULT_TOPICS.driverEvents),
                    valueOr(shipmentNotificationDlt,
                            DEFAULT_TOPICS.shipmentNotificationDlt)
            );
        }

        public String shipment() { return shipmentEvents; }
        public String vehicle() { return vehicleEvents; }
        public String driver() { return driverEvents; }
        public String notificationDlt() { return shipmentNotificationDlt; }
    }

    public record Outbox(
            long pollDelayMs,
            int batchSize,
            int maxAttempts
    ) {
        private Outbox withDefaults() {
            return new Outbox(
                    pollDelayMs > 0 ? pollDelayMs : 1000,
                    batchSize > 0 ? batchSize : 50,
                    maxAttempts > 0 ? maxAttempts : 10
            );
        }
    }

    private static String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
