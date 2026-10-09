
package com.fleetflow.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.customer.dto.CustomerAddressRequest;
import com.fleetflow.customer.dto.CustomerCreateRequest;
import com.fleetflow.customer.dto.CustomerResponse;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.service.CustomerService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private CustomerAddressRequest validAddress() {
        return new CustomerAddressRequest(
                "12 Industrial Road",
                null,
                "Bhilai",
                "Chhattisgarh",
                "490001",
                "India"
        );
    }

    private CustomerCreateRequest validCreateRequest() {
        return new CustomerCreateRequest(
                CustomerType.BUSINESS,
                "Acme Logistics",
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                null,
                validAddress(),
                null
        );
    }

    private CustomerResponse customerResponse() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return new CustomerResponse(
                UUID.randomUUID(),
                "CUS-AB1234567890",
                CustomerType.BUSINESS,
                "Acme Logistics",
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                null,
                validAddress(),
                null,
                CustomerStatus.ACTIVE,
                now,
                now
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldReturn201() throws Exception {
        CustomerCreateRequest request = validCreateRequest();
        CustomerResponse response = customerResponse();

        when(customerService.create(any(CustomerCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerCode")
                        .value("CUS-AB1234567890"))
                .andExpect(jsonPath("$.customerType")
                        .value("BUSINESS"))
                .andExpect(jsonPath("$.companyName")
                        .value("Acme Logistics"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));

        verify(customerService).create(any(CustomerCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerMustNotListAllCustomers() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(customerService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldRejectInvalidEmail() throws Exception {
        String invalidRequest = """
                {
                  "customerType": "BUSINESS",
                  "companyName": "Acme Logistics",
                  "firstName": "Rahul",
                  "lastName": "Sharma",
                  "email": "not-an-email",
                  "phone": "+919876543210",
                  "taxIdentifier": null,
                  "billingAddress": {
                    "addressLine1": "12 Industrial Road",
                    "addressLine2": null,
                    "city": "Bhilai",
                    "state": "Chhattisgarh",
                    "postalCode": "490001",
                    "country": "India"
                  },
                  "shippingAddress": null
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldRejectMissingCompanyName() throws Exception {
        CustomerCreateRequest request = new CustomerCreateRequest(
                CustomerType.BUSINESS,
                " ",
                "Rahul",
                "Sharma",
                "rahul@acme.example",
                "+919876543210",
                null,
                validAddress(),
                null
        );

        /*
         * Bean Validation does not enforce the conditional rule that
         * BUSINESS requires companyName. That rule currently lives in
         * the service, so this controller test only checks DTO validation.
         */
        when(customerService.create(any(CustomerCreateRequest.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Company name is required for business customers"
                ));

        mockMvc.perform(post("/api/v1/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listShouldReturnPagedCustomers() throws Exception {
        CustomerResponse response = customerResponse();

        when(customerService.findAll(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 20),
                1
        ));

        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].customerCode")
                        .value("CUS-AB1234567890"));
    }

    @Test
    void unauthenticatedRequestShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }
}
