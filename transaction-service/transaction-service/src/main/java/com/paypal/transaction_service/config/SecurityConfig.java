package com.paypal.transaction_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ✅ Allows POST requests from Postman (no CSRF token required)
            .csrf(csrf -> csrf.disable())

            // ✅ Allows all GET/POST under /api/transactions/**
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/transactions/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // ✅ Safe to keep (used later for auth / users / JWT)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}