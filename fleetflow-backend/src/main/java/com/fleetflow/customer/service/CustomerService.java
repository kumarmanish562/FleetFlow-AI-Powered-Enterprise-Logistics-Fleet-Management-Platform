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

    CustomerResponse createCustomer(
            CustomerCreateRequest request
    );

    CustomerResponse getCustomerById(UUID id);

    Page<CustomerResponse> getCustomers(
            CustomerStatus status,
            CustomerType type,
            String search,
            Pageable pageable
    );

    CustomerResponse updateCustomer(
            UUID id,
            CustomerUpdateRequest request
    );

    CustomerResponse updateCustomerStatus(
            UUID id,
            CustomerStatusUpdateRequest request
    );
}