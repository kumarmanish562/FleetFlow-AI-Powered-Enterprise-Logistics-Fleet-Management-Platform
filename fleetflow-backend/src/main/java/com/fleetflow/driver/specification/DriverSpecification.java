package com.fleetflow.driver.specification;

import com.fleetflow.driver.entity.Driver;
import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;
import com.fleetflow.driver.enums.LicenseType;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public final class DriverSpecification {

    private DriverSpecification() {
    }

    public static Specification<Driver> filter(
            String search,
            DriverEmploymentStatus employmentStatus,
            DriverAvailability availability,
            LicenseType licenseType
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (search != null &&
                    !search.isBlank()) {

                String value =
                        "%" +
                                search.trim().toLowerCase() +
                                "%";

                predicates.add(
                        criteriaBuilder.or(

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("firstName")
                                        ),
                                        value
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("lastName")
                                        ),
                                        value
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("licenseNumber")
                                        ),
                                        value
                                ),

                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("phone")
                                        ),
                                        value
                                )
                        )
                );
            }

            if (employmentStatus != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("employmentStatus"),
                                employmentStatus
                        )
                );
            }

            if (availability != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("availability"),
                                availability
                        )
                );
            }

            if (licenseType != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("licenseType"),
                                licenseType
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}