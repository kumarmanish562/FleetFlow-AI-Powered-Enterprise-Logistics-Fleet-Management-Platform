package com.fleetflow.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fleetflow.driver.dto.DriverCreateRequest;
import com.fleetflow.driver.enums.LicenseType;
import com.fleetflow.driver.service.DriverService;
import com.fleetflow.security.CustomUserDetailsService;
import com.fleetflow.security.JwtService;

import org.junit.jupiter.api.Test;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule());

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DriverService driverService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    @Test
    @WithMockUser(roles = "ADMIN")
    void createDriver_shouldReturnCreated() throws Exception {

        // Arrange
        UUID userId = UUID.randomUUID();

        DriverCreateRequest request =
                new DriverCreateRequest(
                        userId,
                        "Rahul",
                        "Sharma",
                        "+919876543210",
                        LocalDate.of(1998, 5, 15),
                        "CG123456789",
                        LicenseType.COMMERCIAL,
                        LocalDate.now().plusYears(2),
                        5,
                        LocalDate.now()
                );

        when(driverService.createDriver(any(DriverCreateRequest.class)))
                .thenReturn(null);


        // Act + Assert
        mockMvc.perform(
                        post("/api/v1/drivers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated());
    }
}