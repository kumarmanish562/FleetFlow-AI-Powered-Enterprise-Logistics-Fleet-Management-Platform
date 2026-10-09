package com.fleetflow.driver.service;

import com.fleetflow.driver.dto.DriverCreateRequest;
import com.fleetflow.driver.entity.Driver;
import com.fleetflow.driver.enums.DriverAvailability;
import com.fleetflow.driver.enums.DriverEmploymentStatus;
import com.fleetflow.driver.enums.LicenseType;
import com.fleetflow.driver.mapper.DriverMapper;
import com.fleetflow.driver.repository.DriverRepository;

import com.fleetflow.user.entity.User;
import com.fleetflow.user.repository.UserRepository;

import com.fleetflow.vehicle.entity.Vehicle;
import com.fleetflow.vehicle.enums.VehicleStatus;
import com.fleetflow.vehicle.repository.VehicleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverMapper driverMapper;

    @InjectMocks
    private DriverServiceImpl driverService;


    private UUID userId;
    private UUID driverId;
    private UUID vehicleId;

    private User user;
    private Driver driver;
    private Vehicle vehicle;


    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        driver = Driver.builder()
                .user(user)
                .firstName("Rahul")
                .lastName("Sharma")
                .phone("+919876543210")
                .licenseNumber("CG123456789")
                .licenseType(LicenseType.HEAVY_MOTOR_VEHICLE)
                .licenseExpiryDate(
                        LocalDate.now().plusYears(2)
                )
                .yearsOfExperience(6)
                .employmentStatus(
                        DriverEmploymentStatus.ACTIVE
                )
                .availability(
                        DriverAvailability.AVAILABLE
                )
                .build();

        vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
    }


    // =========================================================
    // CREATE DRIVER
    // =========================================================

    @Test
    void createDriver_success() {

        DriverCreateRequest request =
                new DriverCreateRequest(
                        userId,
                        "Rahul",
                        "Sharma",
                        "+919876543210",
                        LocalDate.of(1998, 5, 15),
                        "CG123456789",
                        LicenseType.HEAVY_MOTOR_VEHICLE,
                        LocalDate.now().plusYears(2),
                        6,
                        LocalDate.now()
                );

        when(driverRepository.existsByUserId(userId))
                .thenReturn(false);

        when(
                driverRepository.existsByLicenseNumberIgnoreCase(
                        request.licenseNumber()
                )
        ).thenReturn(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(driverRepository.save(any(Driver.class)))
                .thenReturn(driver);

        driverService.createDriver(request);

        verify(driverRepository)
                .existsByUserId(userId);

        verify(driverRepository)
                .existsByLicenseNumberIgnoreCase(
                        request.licenseNumber()
                );

        verify(userRepository)
                .findById(userId);

        verify(driverRepository)
                .save(any(Driver.class));
    }


    @Test
    void createDriver_duplicateLicense() {

        DriverCreateRequest request =
                new DriverCreateRequest(
                        userId,
                        "Rahul",
                        "Sharma",
                        "+919876543210",
                        LocalDate.of(1998, 5, 15),
                        "CG123456789",
                        LicenseType.COMMERCIAL,
                        LocalDate.now().plusYears(2),
                        6,
                        LocalDate.now()
                );

        when(driverRepository.existsByUserId(userId))
                .thenReturn(false);

        when(
                driverRepository.existsByLicenseNumberIgnoreCase(
                        request.licenseNumber()
                )
        ).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> driverService.createDriver(request)
        );

        verify(driverRepository, never())
                .save(any(Driver.class));
    }


    @Test
    void createDriver_userNotFound() {

        DriverCreateRequest request =
                new DriverCreateRequest(
                        userId,
                        "Rahul",
                        "Sharma",
                        "+919876543210",
                        LocalDate.of(1998, 5, 15),
                        "CG123456789",
                        LicenseType.COMMERCIAL,
                        LocalDate.now().plusYears(2),
                        6,
                        LocalDate.now()
                );

        when(driverRepository.existsByUserId(userId))
                .thenReturn(false);

        when(
                driverRepository.existsByLicenseNumberIgnoreCase(
                        request.licenseNumber()
                )
        ).thenReturn(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> driverService.createDriver(request)
        );

        verify(driverRepository, never())
                .save(any(Driver.class));
    }


    @Test
    void createDriver_userAlreadyDriver() {

        DriverCreateRequest request =
                new DriverCreateRequest(
                        userId,
                        "Rahul",
                        "Sharma",
                        "+919876543210",
                        LocalDate.of(1998, 5, 15),
                        "CG123456789",
                        LicenseType.COMMERCIAL,
                        LocalDate.now().plusYears(2),
                        6,
                        LocalDate.now()
                );

        when(driverRepository.existsByUserId(userId))
                .thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> driverService.createDriver(request)
        );

        verify(
                driverRepository,
                never()
        ).existsByLicenseNumberIgnoreCase(anyString());

        verify(
                userRepository,
                never()
        ).findById(any());

        verify(
                driverRepository,
                never()
        ).save(any());
    }


    // =========================================================
    // ASSIGN VEHICLE
    // =========================================================

    @Test
    void assignVehicle_success() {

        driver.setAvailability(
                DriverAvailability.AVAILABLE
        );

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(vehicleId))
                .thenReturn(Optional.of(vehicle));

        when(driverRepository.save(driver))
                .thenReturn(driver);

        driverService.assignVehicle(
                driverId,
                vehicleId
        );

        assertEquals(
                vehicle,
                driver.getAssignedVehicle()
        );

        assertEquals(
                VehicleStatus.ASSIGNED,
                vehicle.getStatus()
        );

        verify(vehicleRepository)
                .findById(vehicleId);

        verify(driverRepository)
                .save(driver);
    }


    @Test
    void assignVehicle_driverNotAvailable() {

        driver.setAvailability(
                DriverAvailability.OFF_DUTY
        );

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.of(driver));

        assertThrows(
                RuntimeException.class,
                () ->
                        driverService.assignVehicle(
                                driverId,
                                vehicleId
                        )
        );

        verify(
                vehicleRepository,
                never()
        ).findById(any());

        verify(
                driverRepository,
                never()
        ).save(any());
    }


    @Test
    void assignVehicle_vehicleUnavailable() {

        vehicle.setStatus(
                VehicleStatus.MAINTENANCE
        );

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(vehicleId))
                .thenReturn(Optional.of(vehicle));

        assertThrows(
                RuntimeException.class,
                () ->
                        driverService.assignVehicle(
                                driverId,
                                vehicleId
                        )
        );

        assertNull(
                driver.getAssignedVehicle()
        );

        verify(
                driverRepository,
                never()
        ).save(any());
    }


    @Test
    void assignVehicle_expiredLicense() {

        driver.setLicenseExpiryDate(
                LocalDate.now().minusDays(1)
        );

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.of(driver));

        assertThrows(
                RuntimeException.class,
                () ->
                        driverService.assignVehicle(
                                driverId,
                                vehicleId
                        )
        );

        verify(
                vehicleRepository,
                never()
        ).findById(any());

        verify(
                driverRepository,
                never()
        ).save(any());
    }


    @Test
    void assignVehicle_driverAlreadyAssigned() {

        driver.setAssignedVehicle(vehicle);

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.of(driver));

        assertThrows(
                RuntimeException.class,
                () ->
                        driverService.assignVehicle(
                                driverId,
                                vehicleId
                        )
        );

        verify(
                vehicleRepository,
                never()
        ).findById(any());

        verify(
                driverRepository,
                never()
        ).save(any());
    }


    @Test
    void assignVehicle_driverNotFound() {

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () ->
                        driverService.assignVehicle(
                                driverId,
                                vehicleId
                        )
        );

        verify(
                vehicleRepository,
                never()
        ).findById(any());
    }


    @Test
    void assignVehicle_vehicleNotFound() {

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(vehicleId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () ->
                        driverService.assignVehicle(
                                driverId,
                                vehicleId
                        )
        );

        assertNull(
                driver.getAssignedVehicle()
        );

        verify(
                driverRepository,
                never()
        ).save(any());
    }


    // =========================================================
    // UNASSIGN VEHICLE
    // =========================================================

    @Test
    void unassignVehicle_success() {

        driver.setAssignedVehicle(vehicle);

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.of(driver));

        when(driverRepository.save(driver))
                .thenReturn(driver);

        driverService.unassignVehicle(driverId);

        assertNull(
                driver.getAssignedVehicle()
        );

        assertEquals(
                VehicleStatus.AVAILABLE,
                vehicle.getStatus()
        );

        verify(driverRepository)
                .findById(driverId);

        verify(driverRepository)
                .save(driver);
    }


    @Test
    void unassignVehicle_driverNotFound() {

        when(driverRepository.findById(driverId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () ->
                        driverService.unassignVehicle(
                                driverId
                        )
        );

        verify(
                driverRepository,
                never()
        ).save(any());
    }
}