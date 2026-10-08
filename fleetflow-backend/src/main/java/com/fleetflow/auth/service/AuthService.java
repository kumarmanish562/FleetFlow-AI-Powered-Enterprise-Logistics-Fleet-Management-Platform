package com.fleetflow.auth.service;

import com.fleetflow.auth.dto.LoginRequest;
import com.fleetflow.auth.dto.LoginResponse;
import com.fleetflow.auth.dto.RefreshTokenRequest;
import com.fleetflow.auth.dto.RegisterRequest;
import com.fleetflow.auth.dto.UserResponse;
import com.fleetflow.common.exception.DuplicateResourceException;
import com.fleetflow.common.exception.ResourceNotFoundException;
import com.fleetflow.security.JwtService;
import com.fleetflow.user.entity.RefreshToken;
import com.fleetflow.user.entity.Role;
import com.fleetflow.user.entity.User;
import com.fleetflow.user.enums.RoleName;
import com.fleetflow.user.enums.UserStatus;
import com.fleetflow.user.repository.RefreshTokenRepository;
import com.fleetflow.user.repository.RoleRepository;
import com.fleetflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RefreshTokenRepository
            refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager
            authenticationManager;

    private final JwtService jwtService;

    @Value("${fleetflow.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${fleetflow.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Transactional
    public UserResponse register(
            RegisterRequest request
    ) {

        String email =
                normalizeEmail(request.email());

        if (
                userRepository
                        .existsByEmailIgnoreCase(email)
        ) {

            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }

        Role customerRole =
                roleRepository
                        .findByName(
                                RoleName.CUSTOMER.name()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "CUSTOMER role not found"
                                )
                        );

        User user =
                User.builder()
                        .email(email)
                        .passwordHash(
                                passwordEncoder.encode(
                                        request.password()
                                )
                        )
                        .firstName(
                                request.firstName().trim()
                        )
                        .lastName(
                                request.lastName() == null
                                        ? null
                                        : request.lastName().trim()
                        )
                        .phone(
                                request.phone() == null
                                        ? null
                                        : request.phone().trim()
                        )
                        .status(
                                UserStatus.ACTIVE
                        )
                        .roles(
                                Set.of(customerRole)
                        )
                        .build();

        User savedUser =
                userRepository.save(user);

        return toUserResponse(savedUser);
    }

    @Transactional
    public LoginResponse login(
            LoginRequest request
    ) {

        String email =
                normalizeEmail(request.email());

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.password()
                        )
                );

        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        UserDetails userDetails =
                (UserDetails)
                        authentication.getPrincipal();

        String accessToken =
                jwtService.generateAccessToken(
                        user.getId().toString(),
                        user.getEmail()
                );

        String refreshToken =
                createRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiration / 1000
        );
    }

    @Transactional
    public LoginResponse refresh(
            RefreshTokenRequest request
    ) {

        String tokenHash =
                hashToken(
                        request.refreshToken()
                );

        RefreshToken storedToken =
                refreshTokenRepository
                        .findByTokenHashAndRevokedFalse(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new SecurityException(
                                        "Invalid refresh token"
                                )
                        );

        if (
                storedToken
                        .getExpiresAt()
                        .isBefore(
                                OffsetDateTime.now(
                                        ZoneOffset.UTC
                                )
                        )
        ) {

            storedToken.setRevoked(true);

            refreshTokenRepository.save(
                    storedToken
            );

            throw new SecurityException(
                    "Refresh token expired"
            );
        }

        User user =
                storedToken.getUser();

        if (
                user.getStatus() != UserStatus.ACTIVE
        ) {

            storedToken.setRevoked(true);

            refreshTokenRepository.save(
                    storedToken
            );

            throw new SecurityException(
                    "User account is not active"
            );
        }

        // Rotation
        storedToken.setRevoked(true);

        refreshTokenRepository.save(
                storedToken
        );

        String newAccessToken =
                jwtService.generateAccessToken(
                        user.getId().toString(),
                        user.getEmail()
                );

        String newRefreshToken =
                createRefreshToken(user);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                accessTokenExpiration / 1000
        );
    }

    @Transactional
    public void logout(
            RefreshTokenRequest request
    ) {

        String tokenHash =
                hashToken(
                        request.refreshToken()
                );

        refreshTokenRepository
                .findByTokenHashAndRevokedFalse(
                        tokenHash
                )
                .ifPresent(token -> {

                    token.setRevoked(true);

                    refreshTokenRepository.save(token);
                });
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(
            String email
    ) {

        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return toUserResponse(user);
    }

    private String createRefreshToken(
            User user
    ) {

        String rawToken =
                generateSecureToken();

        String tokenHash =
                hashToken(rawToken);

        OffsetDateTime expiresAt =
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plus(
                                Duration.ofMillis(
                                        refreshTokenExpiration
                                )
                        );

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .tokenHash(tokenHash)
                        .user(user)
                        .expiresAt(expiresAt)
                        .revoked(false)
                        .build();

        refreshTokenRepository.save(
                refreshToken
        );

        return rawToken;
    }

    private String generateSecureToken() {

        byte[] bytes =
                new byte[64];

        SecureRandom secureRandom =
                new SecureRandom();

        secureRandom.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(
            String token
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (
                NoSuchAlgorithmException exception
        ) {

            throw new IllegalStateException(
                    "SHA-256 algorithm unavailable",
                    exception
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private UserResponse toUserResponse(
            User user
    ) {

        Set<String> roles =
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getStatus().name(),
                roles
        );
    }
}