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

import jakarta.persistence.criteria.Predicate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper) {

        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public CustomerResponse create(CustomerCreateRequest request) {

        validateCompanyName(
                request.customerType(),
                request.companyName()
        );

        String email = normalizeEmail(request.email());

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A customer with this email already exists"
            );
        }

        String customerCode = generateCustomerCode();

        Customer customer = Customer.create(
                customerCode,
                request.customerType(),
                request.companyName(),
                request.firstName(),
                request.lastName(),
                email,
                request.phone(),
                request.taxIdentifier(),
                customerMapper.toAddress(request.billingAddress()),
                customerMapper.toAddress(request.shippingAddress())
        );

        try {
            Customer saved = customerRepository.saveAndFlush(customer);
            return customerMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            // Covers concurrent requests and database uniqueness checks.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer email or code already exists"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(
            String search,
            CustomerStatus status,
            CustomerType type,
            Pageable pageable) {

        Specification<Customer> specification =
                buildSpecification(search, status, type);

        return customerRepository.findAll(
                specification,
                pageable
        ).map(customerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(
            UUID id,
            String authenticatedEmail,
            boolean administrator) {

        Customer customer = getCustomer(id);

        verifyOwnership(
                customer,
                authenticatedEmail,
                administrator
        );

        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse update(
            UUID id,
            CustomerUpdateRequest request,
            String authenticatedEmail,
            boolean administrator) {

        Customer customer = getCustomer(id);

        verifyOwnership(
                customer,
                authenticatedEmail,
                administrator
        );

        if (customer.getStatus() == CustomerStatus.SUSPENDED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A suspended customer cannot be modified"
            );
        }

        validateCompanyName(
                customer.getCustomerType(),
                request.companyName()
        );

        String email = normalizeEmail(request.email());

        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(
                email,
                id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A customer with this email already exists"
            );
        }

        customer.updateProfile(
                request.companyName(),
                request.firstName(),
                request.lastName(),
                email,
                request.phone(),
                request.taxIdentifier(),
                customerMapper.toAddress(request.billingAddress()),
                customerMapper.toAddress(request.shippingAddress())
        );

        try {
            Customer saved = customerRepository.saveAndFlush(customer);
            return customerMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer email conflicts with an existing record"
            );
        }
    }

    @Override
    public CustomerResponse changeStatus(
            UUID id,
            CustomerStatusUpdateRequest request,
            boolean superAdministrator) {

        Customer customer = getCustomer(id);
        CustomerStatus currentStatus = customer.getStatus();
        CustomerStatus newStatus = request.status();

        if (currentStatus == newStatus) {
            return customerMapper.toResponse(customer);
        }

        /*
         * Only a SUPER_ADMIN can reactivate a suspended customer.
         * Ordinary administrators may deactivate or suspend an active
         * customer and reactivate an inactive customer.
         */
        if (currentStatus == CustomerStatus.SUSPENDED
                && !superAdministrator) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a super administrator can change a suspended customer"
            );
        }

        customer.changeStatus(newStatus);

        return customerMapper.toResponse(
                customerRepository.saveAndFlush(customer)
        );
    }

    private Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));
    }

    private void verifyOwnership(
            Customer customer,
            String authenticatedEmail,
            boolean administrator) {

        if (administrator) {
            return;
        }

        if (authenticatedEmail == null
                || customer.getEmail() == null
                || !customer.getEmail().equalsIgnoreCase(
                authenticatedEmail.trim())) {

            // Return 404 to avoid revealing another customer's record.
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Customer not found"
            );
        }
    }

    private Specification<Customer> buildSpecification(
            String search,
            CustomerStatus status,
            CustomerType type) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {

                String pattern = "%"
                        + search.trim().toLowerCase(Locale.ROOT)
                        + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("customerCode")),
                                pattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                pattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("firstName")),
                                pattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("lastName")),
                                pattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                pattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("companyName")),
                                pattern
                        )
                ));
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            if (type != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("customerType"),
                                type
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

    private void validateCompanyName(
            CustomerType customerType,
            String companyName) {

        if (customerType == CustomerType.BUSINESS
                && (companyName == null || companyName.isBlank())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Company name is required for business customers"
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateCustomerCode() {
        /*
         * UUID randomness makes accidental collisions very unlikely.
         * The unique database constraint remains the final guarantee.
         */
        return "CUS-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
    }
}