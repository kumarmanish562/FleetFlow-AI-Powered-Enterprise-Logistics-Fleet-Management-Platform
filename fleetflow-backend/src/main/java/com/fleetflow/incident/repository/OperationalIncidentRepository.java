package com.fleetflow.incident.repository;

import com.fleetflow.incident.entity.OperationalIncident;
import com.fleetflow.incident.enums.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OperationalIncidentRepository extends JpaRepository<OperationalIncident, UUID> {
    Optional<OperationalIncident> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("""
        SELECT i FROM OperationalIncident i
        WHERE i.organizationId=:organizationId
          AND (:customerId IS NULL OR i.shipment.customer.id=:customerId)
          AND i.reportedAt>=:from AND i.reportedAt<:to
        """)
    Page<OperationalIncident> findForOperationalAI(@Param("organizationId") UUID organizationId,
            @Param("customerId") UUID customerId, @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to, Pageable pageable);

    @Query("""
        SELECT i FROM OperationalIncident i
        WHERE i.id=:id AND i.organizationId=:organizationId
          AND (:customerId IS NULL OR i.shipment.customer.id=:customerId)
        """)
    Optional<OperationalIncident> findVisible(@Param("id") UUID id,
            @Param("organizationId") UUID organizationId, @Param("customerId") UUID customerId);
}
