package com.fleetflow.config;

import com.fleetflow.security.CustomUserDetailsService;
import com.fleetflow.security.JwtAccessDeniedHandler;
import com.fleetflow.security.JwtAuthenticationEntryPoint;
import com.fleetflow.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomUserDetailsService userDetailsService;

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    private final JwtAccessDeniedHandler accessDeniedHandler;

    // ============================================================
    // PASSWORD ENCODER
    // ============================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    // ============================================================
    // AUTHENTICATION PROVIDER
    // ============================================================

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }
    // ============================================================
    // AUTHENTICATION MANAGER
    // ============================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // ------------------------------------------------
                // CSRF
                // ------------------------------------------------
                .csrf(csrf ->
                        csrf.disable()
                )

                // ------------------------------------------------
                // STATELESS SESSION
                // ------------------------------------------------
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ------------------------------------------------
                // EXCEPTION HANDLING
                // ------------------------------------------------
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------
                .authorizeHttpRequests(auth ->
                        auth

                                // Public authentication endpoints
                                .requestMatchers(
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh"
                                )
                                .permitAll()

                                // Public health endpoints
                                .requestMatchers(
                                        "/actuator/health",
                                        "/actuator/health/**"
                                )
                                .permitAll()

                                // Public system endpoints
                                .requestMatchers(
                                        "/api/v1/system/**"
                                )
                                .permitAll()

                                // Everything else requires authentication
                                .anyRequest()
                                .authenticated()
                )

                // ------------------------------------------------
                // AUTHENTICATION PROVIDER
                // ------------------------------------------------
                .authenticationProvider(
                        authenticationProvider()
                )

                // ------------------------------------------------
                // JWT FILTER
                // ------------------------------------------------
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}