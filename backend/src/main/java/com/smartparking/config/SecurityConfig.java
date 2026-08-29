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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/api/v1/vehicles/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        UserDetails admin = User
                .withUsername("dev-admin")
                .password("{noop}dev-password")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
