package com.fleetflow.vehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.security.CustomUserDetailsService;
import com.fleetflow.security.JwtService;
import com.fleetflow.vehicle.dto.VehicleCreateRequest;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleStatusUpdateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeSummary;
import com.fleetflow.vehicle.dto.VehicleUpdateRequest;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private final UUID vehicleId =
            UUID.randomUUID();

    private final UUID vehicleTypeId =
            UUID.randomUUID();

    private VehicleResponse response() {

        return new VehicleResponse(
                vehicleId,
                new VehicleTypeSummary(
                        vehicleTypeId,
                        "HEAVY_TRUCK",
                        new BigDecimal("25000")
                ),
                "CG04AB1234",
                "VIN123",
                "Tata",
                "Prima",
                2024,
                FuelType.DIESEL,
                VehicleStatus.AVAILABLE,
                new BigDecimal("1000"),
                null,
                null
        );
    }

    @Test
    void create_shouldReturn201() throws Exception {

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        vehicleTypeId,
                        "CG04AB1234",
                        "VIN123",
                        "Tata",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("1000")
                );

        when(vehicleService.create(request))
                .thenReturn(response());

        mockMvc.perform(
                        post("/api/v1/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.registrationNumber")
                                .value("CG04AB1234")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("AVAILABLE")
                );

        verify(vehicleService)
                .create(request);
    }

    @Test
    void create_invalidRequest_shouldReturn400()
            throws Exception {

        VehicleCreateRequest request =
                new VehicleCreateRequest(
                        null,
                        "",
                        null,
                        "",
                        "",
                        1800,
                        null,
                        new BigDecimal("-1")
                );

        mockMvc.perform(
                        post("/api/v1/vehicles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verify(
                vehicleService,
                never()
        ).create(any());
    }

    @Test
    void findById_shouldReturn200()
            throws Exception {

        when(
                vehicleService.findById(vehicleId)
        ).thenReturn(response());

        mockMvc.perform(
                        get(
                                "/api/v1/vehicles/{id}",
                                vehicleId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(vehicleId.toString())
                );
    }

    @Test
    void findAll_shouldReturn200()
            throws Exception {

        when(
                vehicleService.findAll(
                        any(),
                        any()
                )
        ).thenReturn(
                new PageImpl<>(
                        List.of(response())
                )
        );

        mockMvc.perform(
                        get("/api/v1/vehicles")
                                .param("page", "0")
                                .param("size", "10")
                                .param(
                                        "status",
                                        "AVAILABLE"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content")
                                .isArray()
                );
    }

    @Test
    void update_shouldReturn200()
            throws Exception {

        VehicleUpdateRequest request =
                new VehicleUpdateRequest(
                        vehicleTypeId,
                        "CG04AB1234",
                        "VIN123",
                        "Tata Motors",
                        "Prima",
                        2024,
                        FuelType.DIESEL,
                        new BigDecimal("1500")
                );

        when(
                vehicleService.update(
                        vehicleId,
                        request
                )
        ).thenReturn(response());

        mockMvc.perform(
                        put(
                                "/api/v1/vehicles/{id}",
                                vehicleId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_shouldReturn200()
            throws Exception {

        VehicleStatusUpdateRequest request =
                new VehicleStatusUpdateRequest(
                        VehicleStatus.ASSIGNED
                );

        when(
                vehicleService.updateStatus(
                        vehicleId,
                        request
                )
        ).thenReturn(response());

        mockMvc.perform(
                        patch(
                                "/api/v1/vehicles/{id}/status",
                                vehicleId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204()
            throws Exception {

        doNothing()
                .when(vehicleService)
                .delete(vehicleId);

        mockMvc.perform(
                        delete(
                                "/api/v1/vehicles/{id}",
                                vehicleId
                        )
                )
                .andExpect(status().isNoContent());

        verify(vehicleService)
                .delete(vehicleId);
    }
}