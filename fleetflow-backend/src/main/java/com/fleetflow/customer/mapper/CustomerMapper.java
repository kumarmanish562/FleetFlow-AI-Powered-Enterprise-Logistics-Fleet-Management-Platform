package com.fleetflow.customer.mapper;

import com.fleetflow.customer.dto.CustomerAddressRequest;
import com.fleetflow.customer.dto.CustomerResponse;
import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.entity.CustomerAddress;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getCustomerType(),
                customer.getCompanyName(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getTaxIdentifier(),
                toAddressRequest(customer.getBillingAddress()),
                toAddressRequest(customer.getShippingAddress()),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public CustomerAddress toAddress(
            CustomerAddressRequest request) {

        if (request == null) {
            return null;
        }

        return new CustomerAddress(
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.state(),
                request.postalCode(),
                request.country()
        );
    }

    private CustomerAddressRequest toAddressRequest(
            CustomerAddress address) {

        if (address == null) {
            return null;
        }

        return new CustomerAddressRequest(
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}