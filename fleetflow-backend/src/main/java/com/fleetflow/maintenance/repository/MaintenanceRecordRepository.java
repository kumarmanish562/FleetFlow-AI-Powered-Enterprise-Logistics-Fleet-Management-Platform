package com.fleetflow.maintenance.repository;

import com.fleetflow.maintenance.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MaintenanceRecordRepository
        extends JpaRepository<MaintenanceRecord, UUID> {

    List<MaintenanceRecord>
    findByVehicleIdOrderByMaintenanceDateDesc(
            UUID vehicleId
    );

    List<MaintenanceRecord>
    findByNextServiceDateBefore(
            LocalDate date
    );
}