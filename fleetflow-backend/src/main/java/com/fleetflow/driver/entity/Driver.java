package com.fleetflow.driver.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;
import com.fleetflow.driver.enums.LicenseType;
import com.fleetflow.user.entity.User;
import com.fleetflow.vehicle.entity.Vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_drivers_user",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uk_drivers_license",
                        columnNames = "license_number"
                )
        },
        indexes = {
                @Index(
                        name = "idx_drivers_employment_status",
                        columnList = "employment_status"
                ),
                @Index(
                        name = "idx_drivers_availability",
                        columnList = "availability"
                ),
                @Index(
                        name = "idx_drivers_license_expiry",
                        columnList = "license_expiry_date"
                ),
                @Index(
                        name = "idx_drivers_assigned_vehicle",
                        columnList = "assigned_vehicle_id"
                ),
                @Index(
                        name = "idx_drivers_last_name",
                        columnList = "last_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver extends BaseEntity {

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_drivers_user"
            )
    )
    private User user;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;

    @Column(
            name = "phone",
            nullable = false,
            length = 20
    )
    private String phone;

    @Column(
            name = "date_of_birth"
    )
    private LocalDate dateOfBirth;

    @Column(
            name = "license_number",
            nullable = false,
            length = 100
    )
    private String licenseNumber;

    @Column(
            name = "license_expiry_date",
            nullable = false
    )
    private LocalDate licenseExpiryDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "license_type",
            nullable = false,
            length = 50
    )
    private LicenseType licenseType;

    @Column(
            name = "years_of_experience",
            nullable = false
    )
    private Integer yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "employment_status",
            nullable = false,
            length = 30
    )
    private DriverEmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "availability",
            nullable = false,
            length = 30
    )
    private DriverAvailability availability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_vehicle_id",
            foreignKey = @ForeignKey(
                    name = "fk_drivers_vehicle"
            )
    )
    private Vehicle assignedVehicle;

    @Column(name = "hire_date")
    private LocalDate hireDate;
}