package com.fleetflow.customer.repository;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            UUID id
    );

    @Query("""
        SELECT c
        FROM Customer c
        WHERE
            (:status IS NULL OR c.status = :status)
            AND (:type IS NULL OR c.customerType = :type)
            AND (
                :search IS NULL
                OR LOWER(c.name) LIKE LOWER(
                    CONCAT('%', :search, '%')
                )
                OR LOWER(c.email) LIKE LOWER(
                    CONCAT('%', :search, '%')
                )
                OR c.phone LIKE CONCAT('%', :search, '%')
            )
        """)
    Page<Customer> searchCustomers(
            @Param("status") CustomerStatus status,
            @Param("type") CustomerType type,
            @Param("search") String search,
            Pageable pageable
    );
}