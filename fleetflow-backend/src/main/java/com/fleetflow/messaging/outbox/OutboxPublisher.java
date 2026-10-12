package com.fleetflow.messaging.outbox;

import com.fleetflow.messaging.config.MessagingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxClaimRepository claimRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessagingProperties properties;

    public OutboxPublisher(
            OutboxClaimRepository claimRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            MessagingProperties properties
    ) {
        this.claimRepository = claimRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Scheduled(
            fixedDelayString =
                    "${fleetflow.messaging.outbox.poll-delay-ms:1000}"
    )
    public void publishPendingEvents() {
        List<OutboxClaim> claims = claimRepository.claimBatch(
                properties.outbox().batchSize()
        );

        for (OutboxClaim claim : claims) {
            publishOne(claim);
        }
    }

    private void publishOne(OutboxClaim claim) {
        try {
            kafkaTemplate.send(
                    claim.topic(),
                    claim.messageKey(),
                    claim.payload()
            ).get(30, TimeUnit.SECONDS);

            boolean marked = claimRepository.markPublished(claim);

            if (!marked) {
                log.warn(
                        "Claim expired before completion eventId={}",
                        claim.eventId()
                );
            }
        } catch (Exception exception) {
            String message = exception.getMessage();

            if (message == null || message.isBlank()) {
                message = exception.getClass().getSimpleName();
            }

            if (message.length() > 2000) {
                message = message.substring(0, 2000);
            }

            claimRepository.markFailure(
                    claim,
                    message,
                    properties.outbox().maxAttempts()
            );

            log.error(
                    "Failed publishing eventId={} type={}",
                    claim.eventId(),
                    claim.eventType(),
                    exception
            );
        }
    }
}