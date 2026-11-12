package com.fleetflow.customer.service;

import com.fleetflow.customer.dto.CustomerCreateRequest;
import com.fleetflow.customer.dto.CustomerResponse;
import com.fleetflow.customer.dto.CustomerStatusUpdateRequest;
import com.fleetflow.customer.dto.CustomerUpdateRequest;
import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.mapper.CustomerMapper;
import com.fleetflow.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import com.fleetflow.user.service.CurrentOrganizationProvider;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CurrentOrganizationProvider organizations;

    @Autowired
    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            CurrentOrganizationProvider organizations
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.organizations = organizations;
    }

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this(customerRepository, customerMapper, null);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(
            CustomerCreateRequest request
    ) {
        validateBusinessRules(
                request.customerType(),
                request.name()
        );

        String email = normalizeEmail(request.email());

        if (email != null
                && customerRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A customer with this email already exists"
            );
        }

        Customer customer = customerMapper.toEntity(request);
        if (organizations != null) customer.setOrganizationId(organizations.requireOrganizationId());

        customer.setStatus(CustomerStatus.ACTIVE);

        Customer saved = customerRepository.save(customer);

        return customerMapper.toResponse(saved);
    }

    @Override
    public CustomerResponse getCustomerById(UUID id) {
        return customerMapper.toResponse(findCustomer(id));
    }

    @Override
    public Page<CustomerResponse> getCustomers(
            CustomerStatus status,
            CustomerType type,
            String search,
            Pageable pageable
    ) {
        String normalizedSearch =
                search == null || search.isBlank()
                        ? null
                        : search.trim();

        return customerRepository.searchCustomers(
                status,
                type,
                normalizedSearch,
                pageable
        ).map(customerMapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(
            UUID id,
            CustomerUpdateRequest request
    ) {
        Customer customer = findCustomer(id);

        if (customer.getStatus() == CustomerStatus.BLOCKED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Blocked customers cannot be modified"
            );
        }

        validateBusinessRules(
                request.customerType(),
                request.name()
        );

        String email = normalizeEmail(request.email());

        if (email != null
                && customerRepository
                .existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A customer with this email already exists"
            );
        }

        customerMapper.updateEntity(customer, request);

        return customerMapper.toResponse(
                customerRepository.save(customer)
        );
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomerStatus(
            UUID id,
            CustomerStatusUpdateRequest request
    ) {
        Customer customer = findCustomer(id);

        if (customer.getStatus() == CustomerStatus.BLOCKED
                && request.status() != CustomerStatus.BLOCKED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A blocked customer cannot be reactivated"
            );
        }

        customer.setStatus(request.status());

        return customerMapper.toResponse(
                customerRepository.save(customer)
        );
    }

    private Customer findCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Customer not found: " + id
                        )
                );
    }

    private void validateBusinessRules(
            CustomerType type,
            String name
    ) {
        if (type == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer type is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer name is required"
            );
        }

        /*
         * This version uses one shared name field, matching
         * your existing Customer entity and SQL.
         *
         * If business customers must have a separate companyName,
         * add that field to the entity, DTOs, and database through
         * a new migration before enforcing that rule.
         */
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
