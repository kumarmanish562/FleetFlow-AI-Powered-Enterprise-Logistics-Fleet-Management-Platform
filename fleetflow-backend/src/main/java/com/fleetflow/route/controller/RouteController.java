package com.fleetflow.route.controller;

import com.fleetflow.route.dto.CreateRouteRequest;
import com.fleetflow.route.dto.RouteResponse;
import com.fleetflow.route.enums.RouteStatus;
import com.fleetflow.route.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteResponse createRoute(
            @Valid @RequestBody CreateRouteRequest request) {
        return routeService.createRoute(request);
    }

    @GetMapping("/{id}")
    public RouteResponse getRoute(@PathVariable UUID id) {
        return routeService.getRoute(id);
    }

    @GetMapping
    public Page<RouteResponse> getRoutes(
            @RequestParam(required = false) RouteStatus status,
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable) {

        return routeService.getRoutes(status, pageable);
    }
}