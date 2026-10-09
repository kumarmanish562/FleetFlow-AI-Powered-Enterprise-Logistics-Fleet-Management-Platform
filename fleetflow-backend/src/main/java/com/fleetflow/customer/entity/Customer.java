package com.fleetflow.customer.entity;

import com.fleetflow.common.entity.BaseEntity;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 320)
    private String email;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "customer_type",
            nullable = false,
            length = 30
    )
    private CustomerType customerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerStatus status;

    @Column(
            name = "billing_address",
            columnDefinition = "TEXT"
    )
    private String billingAddress;

    @Column(
            name = "shipping_address",
            columnDefinition = "TEXT"
    )
    private String shippingAddress;
}