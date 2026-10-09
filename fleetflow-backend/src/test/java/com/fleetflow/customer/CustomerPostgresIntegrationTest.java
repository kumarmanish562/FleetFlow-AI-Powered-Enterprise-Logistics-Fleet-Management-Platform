
        package com.fleetflow.customer;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.entity.CustomerAddress;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.repository.CustomerRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@Transactional
class CustomerPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("fleetflow_test")
                    .withUsername("fleetflow")
                    .withPassword("test-password");

    @DynamicPropertySource
    static void configureDatasource(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
        registry.add(
                "spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName
        );
        registry.add(
                "spring.flyway.enabled",
                () -> true
        );
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldPersistCustomer() {
        Customer customer = buildCustomer(
                uniqueCustomerCode(),
                uniqueEmail()
        );

        Customer saved =
                customerRepository.saveAndFlush(customer);

        assertNotNull(saved.getId());

        Customer retrieved = customerRepository
                .findById(saved.getId())
                .orElseThrow();

        assertEquals(saved.getId(), retrieved.getId());
        assertEquals(
                saved.getCustomerCode(),
                retrieved.getCustomerCode()
        );
        assertEquals(
                CustomerStatus.ACTIVE,
                retrieved.getStatus()
        );
        assertEquals("Rahul Sharma", retrieved.getName());
    }

    @Test
    void shouldRejectDuplicateCustomerCode() {
        String customerCode = uniqueCustomerCode();

        Customer first = buildCustomer(
                customerCode,
                uniqueEmail()
        );

        Customer second = buildCustomer(
                customerCode,
                uniqueEmail()
        );

        customerRepository.saveAndFlush(first);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> customerRepository.saveAndFlush(second)
        );
    }

    @Test
    void shouldPersistNormalizedEmail() {
        String email = uniqueEmail().toUpperCase();

        Customer saved = customerRepository.saveAndFlush(
                buildCustomer(uniqueCustomerCode(), email)
        );

        Customer retrieved = customerRepository
                .findById(saved.getId())
                .orElseThrow();

        assertEquals(
                email.toLowerCase(java.util.Locale.ROOT),
                retrieved.getEmail()
        );
    }

    private Customer buildCustomer(
            String customerCode,
            String email
    ) {
        CustomerAddress address = buildAddress();

        return Customer.create(
                customerCode,
                CustomerType.INDIVIDUAL,
                null,
                "Rahul",
                "Sharma",
                email,
                "+919876543210",
                null,
                address,
                address
        );
    }

    private CustomerAddress buildAddress() {
        /*
         * This assumes CustomerAddress has a no-argument
         * constructor and setters for these properties.
         * If it uses a different construction API, adapt
         * this helper to that class.
         */
        CustomerAddress address = new CustomerAddress();
        address.setAddressLine1("12 Industrial Road");
        address.setCity("Bhilai");
        address.setState("Chhattisgarh");
        address.setPostalCode("490001");
        address.setCountry("India");

        return address;
    }

    private String uniqueCustomerCode() {
        return "CUS-" + UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }

    private String uniqueEmail() {
        return "customer-" + UUID.randomUUID()
                + "@example.com";
    }
}
