package com.fleetflow.customer.service;

import com.fleetflow.customer.dto.CustomerAddressRequest;
import com.fleetflow.customer.dto.CustomerCreateRequest;
import com.fleetflow.customer.dto.CustomerResponse;
import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.mapper.CustomerMapper;
import com.fleetflow.customer.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        CustomerAddressRequest billingAddress =
                new CustomerAddressRequest(
                        "12 Industrial Road",
                        null,
                        "Bhilai",
                        "Chhattisgarh",
                        "490001",
                        "India"
                );

        createRequest = new CustomerCreateRequest(
                CustomerType.BUSINESS,
                "Acme Logistics",
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                "GST-EXAMPLE",
                billingAddress,
                null
        );
    }

    @Test
    void createShouldPersistCustomer() {
        when(customerRepository.existsByEmailIgnoreCase(
                "rahul@acme.example"
        )).thenReturn(false);

        when(customerMapper.toAddress(any()))
                .thenReturn(null);

        Customer savedCustomer = Customer.create(
                "CUS-123456789ABC",
                CustomerType.BUSINESS,
                "Acme Logistics",
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                "GST-EXAMPLE",
                null,
                null
        );

        when(customerRepository.saveAndFlush(any(Customer.class)))
                .thenReturn(savedCustomer);

        CustomerResponse expectedResponse =
                mock(CustomerResponse.class);

        when(customerMapper.toResponse(savedCustomer))
                .thenReturn(expectedResponse);

        CustomerResponse response =
                customerService.create(createRequest);

        assertSame(expectedResponse, response);

        verify(customerRepository)
                .existsByEmailIgnoreCase("rahul@acme.example");

        verify(customerRepository)
                .saveAndFlush(any(Customer.class));
    }

    @Test
    void createShouldRejectDuplicateEmail() {
        when(customerRepository.existsByEmailIgnoreCase(
                "rahul@acme.example"
        )).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerService.create(createRequest)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(customerRepository, never())
                .saveAndFlush(any(Customer.class));
    }

    @Test
    void createShouldRejectMissingBusinessCompanyName() {
        CustomerCreateRequest invalidRequest =
                new CustomerCreateRequest(
                        CustomerType.BUSINESS,
                        " ",
                        "Rahul",
                        "Sharma",
                        "rahul@acme.example",
                        "+919876543210",
                        null,
                        createRequest.billingAddress(),
                        null
                );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerService.create(invalidRequest)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(customerRepository);
    }

    @Test
    void findByIdShouldReturnNotFoundForUnknownCustomer() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerService.findById(
                                id,
                                "rahul@acme.example",
                                false
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void findByIdShouldRejectAccessToAnotherCustomer() {
        UUID id = UUID.randomUUID();

        Customer customer = Customer.create(
                "CUS-123456789ABC",
                CustomerType.INDIVIDUAL,
                null,
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                null,
                null,
                null
        );

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> customerService.findById(
                                id,
                                "someone.else@example.com",
                                false
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void findByIdShouldAllowAdministrator() {
        UUID id = UUID.randomUUID();

        Customer customer = Customer.create(
                "CUS-123456789ABC",
                CustomerType.INDIVIDUAL,
                null,
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                null,
                null,
                null
        );

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        CustomerResponse expectedResponse =
                mock(CustomerResponse.class);

        when(customerMapper.toResponse(customer))
                .thenReturn(expectedResponse);

        CustomerResponse result = customerService.findById(
                id,
                "admin@example.com",
                true
        );

        assertSame(expectedResponse, result);
    }

    @Test
    void findAllShouldReturnMappedPage() {
        Customer customer = Customer.create(
                "CUS-123456789ABC",
                CustomerType.INDIVIDUAL,
                null,
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                null,
                null,
                null
        );

        var pageable = PageRequest.of(0, 20);

        Page<Customer> customerPage =
                new PageImpl<>(List.of(customer), pageable, 1);

        when(customerRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(customerPage);

        CustomerResponse expectedResponse =
                mock(CustomerResponse.class);

        when(customerMapper.toResponse(customer))
                .thenReturn(expectedResponse);

        Page<CustomerResponse> result = customerService.findAll(
                "rahul",
                CustomerStatus.ACTIVE,
                CustomerType.INDIVIDUAL,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertSame(expectedResponse, result.getContent().get(0));
    }
}