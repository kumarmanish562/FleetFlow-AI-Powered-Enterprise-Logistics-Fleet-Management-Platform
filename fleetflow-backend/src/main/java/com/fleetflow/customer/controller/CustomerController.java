
        package com.fleetflow.customer.controller;

import com.fleetflow.customer.dto.CustomerCreateRequest;
import com.fleetflow.customer.dto.CustomerResponse;
import com.fleetflow.customer.dto.CustomerStatusUpdateRequest;
import com.fleetflow.customer.dto.CustomerUpdateRequest;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class CustomerController {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "email",
            "createdAt",
            "updatedAt",
            "status"
    );

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Valid @RequestBody CustomerCreateRequest request
    ) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(
            @PathVariable UUID id
    ) {
        return customerService.getCustomerById(id);
    }

    @GetMapping
    public Page<CustomerResponse> getCustomers(
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be zero or greater"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported sort field: " + sortBy
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direction must be asc or desc"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return customerService.getCustomers(
                status,
                type,
                search,
                pageable
        );
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return customerService.updateCustomer(id, request);
    }

    @PatchMapping("/{id}/status")
    public CustomerResponse updateCustomerStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusUpdateRequest request
    ) {
        return customerService.updateCustomerStatus(id, request);
    }
}
