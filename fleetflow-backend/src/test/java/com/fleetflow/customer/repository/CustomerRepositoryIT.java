package com.fleetflow.customer.repository;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerType;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class CustomerRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("fleetflow_test")
                    .withUsername("fleetflow")
                    .withPassword("test-password");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry) {

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
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldPersistCustomer() {
        Customer customer = Customer.create(
                "CUS-111111111111",
                CustomerType.INDIVIDUAL,
                null,
                "Rahul",
                "Sharma",
                "rahul@example.com",
                "+919876543210",
                null,
                null,
                null
        );

        Customer saved = customerRepository.saveAndFlush(customer);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        assertThat(
                customerRepository.findByEmailIgnoreCase(
                        "RAHUL@example.com"
                )
        ).isPresent();
    }

    @Test
    void shouldRejectDuplicateCustomerCode() {
        Customer first = Customer.create(
                "CUS-222222222222",
                CustomerType.INDIVIDUAL,
                null,
                "First",
                "Customer",
                "first@example.com",
                "+919876543210",
                null,
                null,
                null
        );

        Customer second = Customer.create(
                "CUS-222222222222",
                CustomerType.INDIVIDUAL,
                null,
                "Second",
                "Customer",
                "second@example.com",
                "+919876543211",
                null,
                null,
                null
        );

        customerRepository.saveAndFlush(first);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> customerRepository.saveAndFlush(second)
        );
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        Customer first = Customer.create(
                "CUS-333333333333",
                CustomerType.INDIVIDUAL,
                null,
                "First",
                "Customer",
                "duplicate@example.com",
                "+919876543210",
                null,
                null,
                null
        );

        Customer second = Customer.create(
                "CUS-444444444444",
                CustomerType.INDIVIDUAL,
                null,
                "Second",
                "Customer",
                "DUPLICATE@example.com",
                "+919876543211",
                null,
                null,
                null
        );

        customerRepository.saveAndFlush(first);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> customerRepository.saveAndFlush(second)
        );
    }
}