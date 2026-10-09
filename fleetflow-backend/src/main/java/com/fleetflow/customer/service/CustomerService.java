package com.fleetflow.customer.service;

import com.fleetflow.customer.dto.CustomerCreateRequest;
import com.fleetflow.customer.dto.CustomerResponse;
import com.fleetflow.customer.dto.CustomerStatusUpdateRequest;
import com.fleetflow.customer.dto.CustomerUpdateRequest;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(CustomerCreateRequest request);

    Page<CustomerResponse> findAll(
            String search,
            CustomerStatus status,
            CustomerType type,
            Pageable pageable
    );

    CustomerResponse findById(
            UUID id,
            String authenticatedEmail,
            boolean administrator
    );

    CustomerResponse update(
            UUID id,
            CustomerUpdateRequest request,
            String authenticatedEmail,
            boolean administrator
    );

    CustomerResponse changeStatus(
            UUID id,
            CustomerStatusUpdateRequest request,
            boolean superAdministrator
    );
}