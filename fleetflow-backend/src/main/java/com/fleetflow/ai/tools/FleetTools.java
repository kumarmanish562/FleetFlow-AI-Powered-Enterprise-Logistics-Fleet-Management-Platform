package com.fleetflow.ai.tools;

import com.fleetflow.ai.security.AIDataAccessPolicy;
import com.fleetflow.vehicle.dto.VehicleResponse;
import com.fleetflow.vehicle.dto.VehicleFilter;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.service.VehicleService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Component
public class FleetTools {
    private final VehicleService vehicles;
    private final AIDataAccessPolicy access;

    public FleetTools(VehicleService vehicles, AIDataAccessPolicy access) {
        this.vehicles = vehicles;
        this.access = access;
    }

    @Tool(description = "Read the current status and basic operating details of one vehicle by its UUID. Read-only.")
    public VehicleFact getVehicleStatus(@ToolParam(description = "Vehicle UUID") String vehicleId) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getVehicleStatus", scope -> {
            UUID id = UUID.fromString(vehicleId);
            VehicleResponse v = vehicles.findOperationalById(id, scope.organizationId());
            return new VehicleFact(v.id(), v.registrationNumber(), v.status().name(), v.currentOdometer());
        });
    }

    @Tool(description = "List up to 100 vehicles in non-operational statuses (assigned, in transit, maintenance, out of service, retired). Read-only; reports whether results were truncated.")
    public VehicleListFact getUnavailableVehicles() {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getUnavailableVehicles", scope -> {
            List<VehicleFact> result = new ArrayList<>();
            long total = 0;
            boolean truncated = false;
            for (VehicleStatus status : List.of(VehicleStatus.ASSIGNED, VehicleStatus.IN_TRANSIT,
                    VehicleStatus.MAINTENANCE, VehicleStatus.OUT_OF_SERVICE, VehicleStatus.RETIRED)) {
                int remaining = 100 - result.size();
                if (remaining == 0) { truncated = true; break; }
                var page = vehicles.findOperationalByStatus(status, scope.organizationId(),
                        org.springframework.data.domain.PageRequest.of(0, remaining));
                total += page.getTotalElements();
                for (VehicleResponse v : page.getContent())
                    result.add(new VehicleFact(v.id(), v.registrationNumber(), v.status().name(), v.currentOdometer()));
                truncated |= page.getTotalElements() > page.getNumberOfElements();
            }
            return new VehicleListFact(List.copyOf(result), truncated || total > result.size());
        });
    }

    public record VehicleFact(UUID id, String registrationNumber, String status, java.math.BigDecimal odometer) {}
    public record VehicleListFact(List<VehicleFact> vehicles, boolean truncated) {}
}
