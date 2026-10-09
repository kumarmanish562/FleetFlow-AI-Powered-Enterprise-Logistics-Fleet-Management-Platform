package com.fleetflow.customer.mapper;

import com.fleetflow.customer.dto.CustomerCreateRequest;
import com.fleetflow.customer.dto.CustomerResponse;
import com.fleetflow.customer.dto.CustomerUpdateRequest;
import com.fleetflow.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerCreateRequest request) {
        return Customer.builder()
                .name(request.name().trim())
                .email(normalizeEmail(request.email()))
                .phone(normalizePhone(request.phone()))
                .customerType(request.customerType())
                .billingAddress(request.billingAddress())
                .shippingAddress(request.shippingAddress())
                .build();
    }

    public void updateEntity(
            Customer customer,
            CustomerUpdateRequest request
    ) {
        customer.setName(request.name().trim());
        customer.setEmail(normalizeEmail(request.email()));
        customer.setPhone(normalizePhone(request.phone()));
        customer.setCustomerType(request.customerType());
        customer.setBillingAddress(request.billingAddress());
        customer.setShippingAddress(request.shippingAddress());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCustomerType(),
                customer.getStatus(),
                customer.getBillingAddress(),
                customer.getShippingAddress(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone.trim();
    }
}