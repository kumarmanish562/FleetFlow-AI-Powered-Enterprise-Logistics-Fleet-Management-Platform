package com.fleetflow.driver.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.driver.enums.DriverEmploymentStatus;
import com.fleetflow.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
            nullable = false
    )
    private User user;

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

    @Column(
            name = "license_type",
            nullable = false,
            length = 50
    )
    private String licenseType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "employment_status",
            nullable = false,
            length = 30
    )
    private DriverEmploymentStatus employmentStatus;

    @Column(name = "hire_date")
    private LocalDate hireDate;
}