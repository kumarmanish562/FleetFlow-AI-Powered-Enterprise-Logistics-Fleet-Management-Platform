package com.fleetflow.maintenance.repository;

import com.fleetflow.maintenance.entity.MaintenanceRecord;
import com.fleetflow.maintenance.enums.MaintenanceStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MaintenanceRecordRepository
        extends JpaRepository<MaintenanceRecord, UUID> {

    List<MaintenanceRecord>
    findByVehicleIdOrderByMaintenanceDateDesc(UUID vehicleId);

    Page<MaintenanceRecord> findByVehicleId(
            UUID vehicleId,
            Pageable pageable
    );

    List<MaintenanceRecord> findByStatusAndMaintenanceDateBetween(
            MaintenanceStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    List<MaintenanceRecord> findByStatusInAndMaintenanceDateBefore(
            List<MaintenanceStatus> statuses,
            LocalDate date
    );

    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END
        FROM MaintenanceRecord m
        WHERE m.vehicle.id = :vehicleId
          AND m.safetyCritical = TRUE
          AND m.status IN :statuses
        """)
    boolean hasActiveSafetyCriticalMaintenance(
            @Param("vehicleId") UUID vehicleId,
            @Param("statuses") List<MaintenanceStatus> statuses
    );
}