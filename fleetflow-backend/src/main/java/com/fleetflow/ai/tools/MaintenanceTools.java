package com.fleetflow.ai.tools;

import com.fleetflow.ai.security.AIDataAccessPolicy;
import com.fleetflow.maintenance.service.MaintenanceService;
import com.fleetflow.vehicle.service.VehicleService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class MaintenanceTools {
    private final MaintenanceService maintenance;
    private final AIDataAccessPolicy access;
    private final VehicleService vehicles;

    public MaintenanceTools(MaintenanceService maintenance, AIDataAccessPolicy access, VehicleService vehicles) {
        this.maintenance = maintenance;
        this.access = access;
        this.vehicles = vehicles;
    }

    @Tool(description = "Read up to ten recent maintenance records for one vehicle UUID. Read-only; excludes cost, root cause, and parts detail.")
    public List<MaintenanceFact> getVehicleMaintenance(@ToolParam(description = "Vehicle UUID") String vehicleId) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getVehicleMaintenance", scope -> {
            UUID id = UUID.fromString(vehicleId);
            vehicles.findOperationalById(id, scope.organizationId());
            return maintenance.getVehicleHistory(id, PageRequest.of(0, 10)).getContent().stream()
                        .map(m -> new MaintenanceFact(m.id(), m.maintenanceType().name(), m.status().name(),
                                m.maintenanceDate(), m.nextServiceDate(), m.safetyCritical()))
                        .toList();
        });
    }

    public record MaintenanceFact(UUID id, String type, String status, LocalDate date,
                                  LocalDate nextServiceDate, boolean safetyCritical) {}
}
