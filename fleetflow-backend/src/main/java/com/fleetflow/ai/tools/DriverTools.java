package com.fleetflow.ai.tools;

import com.fleetflow.ai.security.AIDataAccessPolicy;
import com.fleetflow.driver.service.DriverService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.PageRequest;

@Component
public class DriverTools {
    private final DriverService drivers;
    private final AIDataAccessPolicy access;

    public DriverTools(DriverService drivers, AIDataAccessPolicy access) {
        this.drivers = drivers;
        this.access = access;
    }

    @Tool(description = "Read a driver's employment, availability and assigned vehicle status by driver UUID. Personal contact and license details are excluded.")
    public DriverFact getDriverAvailability(@ToolParam(description = "Driver UUID") String driverId) {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getDriverAvailability", scope -> {
            UUID id = UUID.fromString(driverId);
            var driver = drivers.getOperationalDriver(id, scope.organizationId());
            return new DriverFact(driver.id(), driver.employmentStatus().name(), driver.availability().name(),
                    driver.assignedVehicleId());
        });
    }

    @Tool(description = "List up to 100 currently available active drivers. Personal contact and license details are excluded; reports whether results were truncated.")
    public AvailableDriversFact getAvailableDrivers() {
        return access.read(SecurityContextHolder.getContext().getAuthentication(), "getAvailableDrivers", scope -> {
            var page = drivers.getOperationalAvailableDrivers(scope.organizationId(), PageRequest.of(0, 50));
            var entries = page.getContent().stream().map(d -> new DriverFact(d.id(),
                    d.employmentStatus().name(), d.availability().name(), d.assignedVehicleId())).toList();
            return new AvailableDriversFact(entries, page.getTotalElements() > entries.size());
        });
    }

    public record DriverFact(UUID id, String employmentStatus, String availability, UUID assignedVehicleId) {}
    public record AvailableDriversFact(List<DriverFact> drivers, boolean truncated) {}
}
