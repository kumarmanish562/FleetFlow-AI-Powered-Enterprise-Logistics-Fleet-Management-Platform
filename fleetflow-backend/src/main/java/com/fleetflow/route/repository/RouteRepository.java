package com.fleetflow.route.repository;

import com.fleetflow.route.entity.Route;
import com.fleetflow.route.enums.RouteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    Optional<Route> findByRouteNumber(String routeNumber);

    boolean existsByRouteNumber(String routeNumber);

    Optional<Route> findByShipment_Id(UUID shipmentId);

    boolean existsByShipment_Id(UUID shipmentId);

    Page<Route> findByStatus(RouteStatus status, Pageable pageable);
}