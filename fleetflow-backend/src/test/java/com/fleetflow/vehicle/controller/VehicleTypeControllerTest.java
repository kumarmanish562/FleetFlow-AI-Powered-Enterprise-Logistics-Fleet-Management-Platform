package com.fleetflow.vehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetflow.security.CustomUserDetailsService;
import com.fleetflow.security.JwtService;
import com.fleetflow.vehicle.dto.VehicleTypeCreateRequest;
import com.fleetflow.vehicle.dto.VehicleTypeResponse;
import com.fleetflow.vehicle.dto.VehicleTypeUpdateRequest;
import com.fleetflow.vehicle.service.VehicleTypeService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehicleTypeService vehicleTypeService;

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

    private final UUID typeId =
            UUID.randomUUID();

    private VehicleTypeResponse response() {

        return new VehicleTypeResponse(
                typeId,
                "HEAVY_TRUCK",
                "Heavy truck",
                new BigDecimal("25000"),
                null,
                null
        );
    }

    @Test
    void create_shouldReturn201()
            throws Exception {

        VehicleTypeCreateRequest request =
                new VehicleTypeCreateRequest(
                        "HEAVY_TRUCK",
                        "Heavy truck",
                        new BigDecimal("25000")
                );

        when(
                vehicleTypeService.create(request)
        ).thenReturn(response());

        mockMvc.perform(
                        post("/api/v1/vehicle-types")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.name")
                                .value("HEAVY_TRUCK")
                );
    }

    @Test
    void create_invalidRequest_shouldReturn400()
            throws Exception {

        VehicleTypeCreateRequest request =
                new VehicleTypeCreateRequest(
                        "",
                        "",
                        BigDecimal.ZERO
                );

        mockMvc.perform(
                        post("/api/v1/vehicle-types")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verify(
                vehicleTypeService,
                never()
        ).create(any());
    }

    @Test
    void findAll_shouldReturn200()
            throws Exception {

        when(
                vehicleTypeService.findAll(any())
        ).thenReturn(
                new PageImpl<>(
                        List.of(response())
                )
        );

        mockMvc.perform(
                        get("/api/v1/vehicle-types")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content")
                                .isArray()
                );
    }

    @Test
    void findById_shouldReturn200()
            throws Exception {

        when(
                vehicleTypeService.findById(typeId)
        ).thenReturn(response());

        mockMvc.perform(
                        get(
                                "/api/v1/vehicle-types/{id}",
                                typeId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(typeId.toString())
                );
    }

    @Test
    void update_shouldReturn200()
            throws Exception {

        VehicleTypeUpdateRequest request =
                new VehicleTypeUpdateRequest(
                        "LIGHT_TRUCK",
                        "Light truck",
                        new BigDecimal("10000")
                );

        when(
                vehicleTypeService.update(
                        typeId,
                        request
                )
        ).thenReturn(response());

        mockMvc.perform(
                        put(
                                "/api/v1/vehicle-types/{id}",
                                typeId
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
                .when(vehicleTypeService)
                .delete(typeId);

        mockMvc.perform(
                        delete(
                                "/api/v1/vehicle-types/{id}",
                                typeId
                        )
                )
                .andExpect(status().isNoContent());

        verify(vehicleTypeService)
                .delete(typeId);
    }
}