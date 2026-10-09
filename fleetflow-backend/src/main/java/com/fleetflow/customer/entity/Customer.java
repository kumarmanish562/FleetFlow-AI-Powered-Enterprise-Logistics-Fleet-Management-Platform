
package com.fleetflow.customer.entity;

import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customers_customer_code",
                        columnNames = "customer_code"
                )
        }
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "customer_code",
            nullable = false,
            length = 40,
            updatable = false
    )
    private String customerCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "customer_type",
            nullable = false,
            length = 30
    )
    private CustomerType customerType;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // Retained for compatibility with the original database schema.
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "tax_identifier", length = 100)
    private String taxIdentifier;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "addressLine1",
                    column = @Column(
                            name = "billing_address_line1",
                            length = 200
                    )
            ),
            @AttributeOverride(
                    name = "addressLine2",
                    column = @Column(
                            name = "billing_address_line2",
                            length = 200
                    )
            ),
            @AttributeOverride(
                    name = "city",
                    column = @Column(
                            name = "billing_city",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "state",
                    column = @Column(
                            name = "billing_state",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "postalCode",
                    column = @Column(
                            name = "billing_postal_code",
                            length = 20
                    )
            ),
            @AttributeOverride(
                    name = "country",
                    column = @Column(
                            name = "billing_country",
                            length = 100
                    )
            )
    })
    private CustomerAddress billingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "addressLine1",
                    column = @Column(
                            name = "shipping_address_line1",
                            length = 200
                    )
            ),
            @AttributeOverride(
                    name = "addressLine2",
                    column = @Column(
                            name = "shipping_address_line2",
                            length = 200
                    )
            ),
            @AttributeOverride(
                    name = "city",
                    column = @Column(
                            name = "shipping_city",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "state",
                    column = @Column(
                            name = "shipping_state",
                            length = 100
                    )
            ),
            @AttributeOverride(
                    name = "postalCode",
                    column = @Column(
                            name = "shipping_postal_code",
                            length = 20
                    )
            ),
            @AttributeOverride(
                    name = "country",
                    column = @Column(
                            name = "shipping_country",
                            length = 100
                    )
            )
    })
    private CustomerAddress shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CustomerStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Lombok builder constructor.
     *
     * Includes legacy name support for existing test fixtures.
     */
    @Builder
    private Customer(
            UUID id,
            String customerCode,
            CustomerType customerType,
            String companyName,
            String firstName,
            String lastName,
            String name,
            String email,
            String phone,
            String taxIdentifier,
            CustomerAddress billingAddress,
            CustomerAddress shippingAddress,
            CustomerStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.customerCode = normalizeOptional(customerCode);
        this.customerType = customerType;
        this.companyName = normalizeOptional(companyName);

        String normalizedName = normalizeOptional(name);
        String normalizedFirstName = normalizeOptional(firstName);
        String normalizedLastName = normalizeOptional(lastName);

        if (normalizedName != null) {
            String[] parts = splitName(normalizedName);

            if (normalizedFirstName == null) {
                normalizedFirstName = parts[0];
            }

            if (normalizedLastName == null) {
                normalizedLastName = parts[1];
            }
        }

        this.firstName = normalizedFirstName;
        this.lastName = normalizedLastName;
        this.email = normalizeEmail(email);
        this.phone = normalizeOptional(phone);
        this.taxIdentifier = normalizeOptional(taxIdentifier);
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;

        this.status = status != null
                ? status
                : CustomerStatus.ACTIVE;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        if (this.firstName != null && this.lastName != null) {
            synchronizeLegacyName();
        } else {
            this.name = normalizedName;
        }
    }

    /**
     * Preferred domain creation method for production code.
     */
    public static Customer create(
            String customerCode,
            CustomerType customerType,
            String companyName,
            String firstName,
            String lastName,
            String email,
            String phone,
            String taxIdentifier,
            CustomerAddress billingAddress,
            CustomerAddress shippingAddress
    ) {
        if (customerType == null) {
            throw new IllegalArgumentException(
                    "Customer type is required"
            );
        }

        if (customerCode == null || customerCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer code is required"
            );
        }

        validateCompanyName(customerType, companyName);

        return new Customer(
                null,
                customerCode.trim(),
                customerType,
                companyName,
                firstName,
                lastName,
                null,
                email,
                phone,
                taxIdentifier,
                billingAddress,
                shippingAddress,
                CustomerStatus.ACTIVE,
                null,
                null
        );
    }

    /**
     * Updates the customer's profile.
     */
    public void updateProfile(
            String companyName,
            String firstName,
            String lastName,
            String email,
            String phone,
            String taxIdentifier,
            CustomerAddress billingAddress,
            CustomerAddress shippingAddress
    ) {
        if (status == CustomerStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "A suspended customer cannot be modified"
            );
        }

        validateCompanyName(customerType, companyName);

        this.companyName = normalizeOptional(companyName);
        this.firstName = normalizeRequired(firstName);
        this.lastName = normalizeRequired(lastName);
        this.email = normalizeEmail(email);
        this.phone = normalizeOptional(phone);
        this.taxIdentifier = normalizeOptional(taxIdentifier);
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;

        synchronizeLegacyName();
    }

    /**
     * Changes the customer's status.
     */
    public void changeStatus(CustomerStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Customer status is required"
            );
        }

        this.status = newStatus;
    }

    private static void validateCompanyName(
            CustomerType type,
            String companyName
    ) {
        if (type == CustomerType.BUSINESS
                && (companyName == null || companyName.isBlank())) {
            throw new IllegalArgumentException(
                    "Company name is required for business customers"
            );
        }
    }

    /**
     * Keeps the legacy name column synchronized.
     */
    private void synchronizeLegacyName() {
        if (firstName == null || lastName == null) {
            return;
        }

        String fullName = firstName + " " + lastName;

        this.name = fullName.length() <= 200
                ? fullName
                : fullName.substring(0, 200);
    }

    /**
     * Splits a legacy full name at its final space.
     */
    private static String[] splitName(String fullName) {
        int separator = fullName.lastIndexOf(' ');

        if (separator > 0 && separator < fullName.length() - 1) {
            return new String[]{
                    fullName.substring(0, separator).trim(),
                    fullName.substring(separator + 1).trim()
            };
        }

        return new String[]{fullName, "Customer"};
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Required fields cannot be blank"
            );
        }

        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private static String normalizeEmail(String email) {
        return email == null || email.isBlank()
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Initializes timestamps before the first insert.
     */
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
        synchronizeLegacyName();
    }

    /**
     * Updates the timestamp before an update.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
        synchronizeLegacyName();
    }

    /**
     * Keeps legacy name and first/last name synchronized when name is set.
     */
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", customerCode='" + customerCode + '\'' +
                ", customerType=" + customerType +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}