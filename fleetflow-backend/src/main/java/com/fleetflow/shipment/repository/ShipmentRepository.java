package com.fleetflow.shipment.repository;

import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByShipmentNumber(String shipmentNumber);

    boolean existsByShipmentNumber(String shipmentNumber);

    Page<Shipment> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    List<Shipment> findByDriverId(UUID driverId);

    Page<Shipment> findByCustomerIdAndStatus(
            UUID customerId, ShipmentStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shipment s where s.id = :id")
    Optional<Shipment> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select count(s)
            from Shipment s
            where s.driver.id = :driverId
              and s.status in :statuses
            """)
    long countActiveShipmentsForDriver(
            @Param("driverId") UUID driverId,
            @Param("statuses") List<ShipmentStatus> statuses);

    @Query("""
            select count(s)
            from Shipment s
            where s.vehicle.id = :vehicleId
              and s.status in :statuses
            """)
    long countActiveShipmentsForVehicle(
            @Param("vehicleId") UUID vehicleId,
            @Param("statuses") List<ShipmentStatus> statuses);
}
