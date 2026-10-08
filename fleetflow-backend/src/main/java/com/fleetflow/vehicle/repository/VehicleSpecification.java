package com.fleetflow.vehicle.repository;

import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class VehicleSpecification {

    private VehicleSpecification() {
    }

    public static Specification<Vehicle> search(
            String search
    ) {

        return (root, query, criteriaBuilder) -> {

            if (
                    search == null
                            || search.isBlank()
            ) {

                return null;
            }

            String value =
                    "%"
                            + search.trim().toLowerCase()
                            + "%";

            return criteriaBuilder.or(

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get(
                                            "registrationNumber"
                                    )
                            ),
                            value
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get(
                                            "vin"
                                    )
                            ),
                            value
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get(
                                            "manufacturer"
                                    )
                            ),
                            value
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get(
                                            "model"
                                    )
                            ),
                            value
                    )
            );
        };
    }

    public static Specification<Vehicle> hasVehicleType(
            UUID vehicleTypeId
    ) {

        return (root, query, criteriaBuilder) -> {

            if (vehicleTypeId == null) {
                return null;
            }

            return criteriaBuilder.equal(
                    root.get("vehicleType")
                            .get("id"),
                    vehicleTypeId
            );
        };
    }

    public static Specification<Vehicle> hasStatus(
            VehicleStatus status
    ) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) {
                return null;
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<Vehicle> hasFuelType(
            FuelType fuelType
    ) {

        return (root, query, criteriaBuilder) -> {

            if (fuelType == null) {
                return null;
            }

            return criteriaBuilder.equal(
                    root.get("fuelType"),
                    fuelType
            );
        };
    }

    public static Specification<Vehicle> hasManufacturer(
            String manufacturer
    ) {

        return (root, query, criteriaBuilder) -> {

            if (
                    manufacturer == null
                            || manufacturer.isBlank()
            ) {

                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.get("manufacturer")
                    ),
                    "%"
                            + manufacturer.trim().toLowerCase()
                            + "%"
            );
        };
    }

    public static Specification<Vehicle> hasModel(
            String model
    ) {

        return (root, query, criteriaBuilder) -> {

            if (
                    model == null
                            || model.isBlank()
            ) {

                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.get("model")
                    ),
                    "%"
                            + model.trim().toLowerCase()
                            + "%"
            );
        };
    }
}