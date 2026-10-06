package com.fleetflow.database;

import com.fleetflow.customer.entity.Customer;
import com.fleetflow.customer.enums.CustomerStatus;
import com.fleetflow.customer.enums.CustomerType;
import com.fleetflow.customer.repository.CustomerRepository;
import com.fleetflow.shipment.entity.Shipment;
import com.fleetflow.shipment.enums.ShipmentPriority;
import com.fleetflow.shipment.enums.ShipmentStatus;
import com.fleetflow.shipment.repository.ShipmentRepository;
import com.fleetflow.support.PostgresContainerTest;
import com.fleetflow.user.entity.User;
import com.fleetflow.user.enums.UserStatus;
import com.fleetflow.user.repository.UserRepository;
import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.entity.VehicleType;
import com.fleetflow.vehicle.enums.FuelType;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.repository.VehicleRepository;
import com.fleetflow.vehicle.repository.VehicleTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DatabaseConstraintTest
        extends PostgresContainerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldRejectDuplicateUserEmail() {

        User firstUser = User.builder()
                .email("duplicate@example.com")
                .passwordHash("password")
                .firstName("First")
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.saveAndFlush(firstUser);

        User secondUser = User.builder()
                .email("duplicate@example.com")
                .passwordHash("password")
                .firstName("Second")
                .status(UserStatus.ACTIVE)
                .build();

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(secondUser)
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectDuplicateVehicleRegistration() {

        VehicleType type =
                vehicleTypeRepository.saveAndFlush(
                        VehicleType.builder()
                                .name("VAN")
                                .description("Delivery van")
                                .maxLoadKg(
                                        new BigDecimal("2000")
                                )
                                .build()
                );

        Vehicle first =
                Vehicle.builder()
                        .vehicleType(type)
                        .registrationNumber(
                                "CG04XY1234"
                        )
                        .vin("VIN-FIRST")
                        .manufacturer("Tata")
                        .model("Ace")
                        .fuelType(FuelType.DIESEL)
                        .status(
                                VehicleStatus.AVAILABLE
                        )
                        .currentOdometer(
                                BigDecimal.ZERO
                        )
                        .build();

        vehicleRepository.saveAndFlush(first);

        Vehicle second =
                Vehicle.builder()
                        .vehicleType(type)
                        .registrationNumber(
                                "CG04XY1234"
                        )
                        .vin("VIN-SECOND")
                        .manufacturer("Mahindra")
                        .model("Bolero")
                        .fuelType(FuelType.DIESEL)
                        .status(
                                VehicleStatus.AVAILABLE
                        )
                        .currentOdometer(
                                BigDecimal.ZERO
                        )
                        .build();

        assertThatThrownBy(() ->
                vehicleRepository.saveAndFlush(second)
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectDuplicateVehicleVin() {

        VehicleType type =
                vehicleTypeRepository.saveAndFlush(
                        VehicleType.builder()
                                .name("TRUCK")
                                .description("Truck")
                                .maxLoadKg(
                                        new BigDecimal("10000")
                                )
                                .build()
                );

        Vehicle first =
                Vehicle.builder()
                        .vehicleType(type)
                        .registrationNumber(
                                "CG01AA1111"
                        )
                        .vin("DUPLICATE-VIN")
                        .manufacturer("Tata")
                        .model("Truck")
                        .fuelType(FuelType.DIESEL)
                        .status(
                                VehicleStatus.AVAILABLE
                        )
                        .currentOdometer(
                                BigDecimal.ZERO
                        )
                        .build();

        vehicleRepository.saveAndFlush(first);

        Vehicle second =
                Vehicle.builder()
                        .vehicleType(type)
                        .registrationNumber(
                                "CG01AA2222"
                        )
                        .vin("DUPLICATE-VIN")
                        .manufacturer("Tata")
                        .model("Truck 2")
                        .fuelType(FuelType.DIESEL)
                        .status(
                                VehicleStatus.AVAILABLE
                        )
                        .currentOdometer(
                                BigDecimal.ZERO
                        )
                        .build();

        assertThatThrownBy(() ->
                vehicleRepository.saveAndFlush(second)
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void shouldRejectDuplicateShipmentNumber() {

        Customer customer =
                customerRepository.saveAndFlush(
                        Customer.builder()
                                .name("Shipment Customer")
                                .email(
                                        "shipment@example.com"
                                )
                                .customerType(
                                        CustomerType.BUSINESS
                                )
                                .status(
                                        CustomerStatus.ACTIVE
                                )
                                .build()
                );

        Shipment first =
                Shipment.builder()
                        .customer(customer)
                        .shipmentNumber("SHP-DUP-001")
                        .status(
                                ShipmentStatus.CREATED
                        )
                        .priority(
                                ShipmentPriority.NORMAL
                        )
                        .originAddress("Bhilai")
                        .destinationAddress("Raipur")
                        .build();

        shipmentRepository.saveAndFlush(first);

        Shipment second =
                Shipment.builder()
                        .customer(customer)
                        .shipmentNumber("SHP-DUP-001")
                        .status(
                                ShipmentStatus.CREATED
                        )
                        .priority(
                                ShipmentPriority.NORMAL
                        )
                        .originAddress("Bhilai")
                        .destinationAddress("Durg")
                        .build();

        assertThatThrownBy(() ->
                shipmentRepository.saveAndFlush(second)
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }
}