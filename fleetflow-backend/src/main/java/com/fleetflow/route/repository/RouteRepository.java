package com.fleetflow.route.repository;

import com.fleetflow.route.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RouteRepository
        extends JpaRepository<Route, UUID> {

    Optional<Route> findByRouteNumber(
            String routeNumber
    );

    boolean existsByRouteNumber(
            String routeNumber
    );
}