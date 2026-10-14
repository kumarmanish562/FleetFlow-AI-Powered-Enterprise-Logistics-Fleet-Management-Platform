package com.fleetflow.billing.repository;

import com.fleetflow.billing.entity.PricingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PricingPolicyRepository
        extends JpaRepository<PricingPolicy, UUID> {

    Optional<PricingPolicy> findFirstByActiveTrueOrderByVersionDesc();

    boolean existsByPolicyNameAndVersion(String policyName, int version);
}