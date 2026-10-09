package com.smartparking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .httpBasic(httpBasic -> {})

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(
                                "/actuator/health",
                                "/api/v1/vehicles/**",
                                "/api/v1/parking/**"
                        ).permitAll()

                        // ADMIN-only gate override operations
                        .requestMatchers(
                                "/api/v1/gates/*/manual-open",
                                "/api/v1/gates/*/emergency-open"
                        ).hasRole("ADMIN")

                        // ADMIN-only admin APIs
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        // All remaining endpoints require authentication
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        UserDetails admin = User
                .withUsername("abhay")
                .password("{noop}abhay123")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}