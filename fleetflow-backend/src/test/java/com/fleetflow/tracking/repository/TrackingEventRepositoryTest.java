package com.fleetflow.tracking.repository;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.tracking.entity.TrackingEvent;
import com.fleetflow.tracking.enums.TrackingEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class TrackingEventRepositoryTest extends PostgresContainerTest {

    @Autowired
    private TrackingEventRepository trackingEventRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Shipment shipment;

    @BeforeEach
    void setUp() {

        /*
         * Remove old tracking events.
         */
        trackingEventRepository.deleteAll();

        /*
         * Create a valid Customer.
         */
        Customer customer =
                Customer.builder()
                        .name("Test Customer")
                        .email("tracking-test@fleetflow.com")
                        .phone("9876543210")
                        .customerType(CustomerType.BUSINESS)
                        .status(CustomerStatus.ACTIVE)
                        .billingAddress("Bhilai, Chhattisgarh")
                        .shippingAddress("Raipur, Chhattisgarh")
                        .build();

        customer =
                customerRepository.saveAndFlush(customer);

        /*
         * Create a valid Shipment.
         *
         * Customer is required by Shipment.
         */
        shipment =
                Shipment.builder()
                        .customer(customer)
                        .shipmentNumber("TEST-TRACK-001")
                        .status(ShipmentStatus.IN_TRANSIT)
                        .priority(ShipmentPriority.NORMAL)
                        .originAddress("Bhilai")
                        .destinationAddress("Raipur")
                        .build();

        shipment =
                shipmentRepository.saveAndFlush(shipment);
    }

    @Test
    void shouldReturnTrackingEventsNewestFirst() {

        /*
         * Older tracking event.
         */
        OffsetDateTime olderTime =
                OffsetDateTime.now().minusHours(2);

        /*
         * Newer tracking event.
         */
        OffsetDateTime newerTime =
                OffsetDateTime.now();

        TrackingEvent first =
                TrackingEvent.builder()
                        .shipment(shipment)
                        .eventType(TrackingEventType.PICKED_UP)
                        .eventTime(olderTime)
                        .locationName("Bhilai")
                        .build();

        TrackingEvent second =
                TrackingEvent.builder()
                        .shipment(shipment)
                        .eventType(TrackingEventType.IN_TRANSIT)
                        .eventTime(newerTime)
                        .locationName("Raipur")
                        .build();

        /*
         * Save both tracking events.
         */
        trackingEventRepository.saveAndFlush(first);
        trackingEventRepository.saveAndFlush(second);

        /*
         * Find events ordered from newest to oldest.
         */
        List<TrackingEvent> events =
                trackingEventRepository
                        .findByShipmentIdOrderByEventTimeDesc(
                                shipment.getId()
                        );

        /*
         * Verify two events were returned.
         */
        assertThat(events)
                .hasSize(2);

        /*
         * Newest event must be first.
         */
        assertThat(events.get(0).getEventType())
                .isEqualTo(
                        TrackingEventType.IN_TRANSIT
                );

        /*
         * Older event must be second.
         */
        assertThat(events.get(1).getEventType())
                .isEqualTo(
                        TrackingEventType.PICKED_UP
                );

        /*
         * Verify the ordering by time.
         */
        assertThat(events.get(0).getEventTime())
                .isAfter(
                        events.get(1).getEventTime()
                );
    }
}