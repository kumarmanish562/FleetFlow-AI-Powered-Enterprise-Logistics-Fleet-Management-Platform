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
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "firstName",
            "lastName",
            "name",
            "email",
            "customerCode"
    );

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CustomerCreateRequest request) {

        CustomerResponse response = customerService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Page<CustomerResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be zero or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported sort field"
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

        return customerService.findAll(
                search,
                status,
                type,
                pageable
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CUSTOMER')")
    public CustomerResponse findById(
            @PathVariable UUID id,
            Authentication authentication) {

        return customerService.findById(
                id,
                authentication.getName(),
                isAdministrator(authentication)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CUSTOMER')")
    public CustomerResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request,
            Authentication authentication) {

        return customerService.update(
                id,
                request,
                authentication.getName(),
                isAdministrator(authentication)
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public CustomerResponse changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusUpdateRequest request,
            Authentication authentication) {

        return customerService.changeStatus(
                id,
                request,
                isSuperAdministrator(authentication)
        );
    }

    private boolean isAdministrator(
            Authentication authentication) {

        return hasAuthority(authentication, "ROLE_ADMIN")
                || isSuperAdministrator(authentication);
    }

    private boolean isSuperAdministrator(
            Authentication authentication) {

        return hasAuthority(authentication, "ROLE_SUPER_ADMIN");
    }

    private boolean hasAuthority(
            Authentication authentication,
            String authority) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(item ->
                        item.getAuthority().equals(authority)
                );
    }
}