package com.bank.transaction_service.config;

import com.bank.transaction_service.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 🔥 INTERNAL - Only for service-to-service (future: add service auth)
                        .requestMatchers("/api/internal/**").permitAll()

                        // 🔥 ADMIN - Admin approval endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 🔥 CUSTOMER - Customer transactions (requires authentication)
                        .requestMatchers("/api/customer/**").authenticated()
                        .requestMatchers("/api/beneficiaries/**").authenticated()
                        .requestMatchers("/api/transactions/**").authenticated()

                        // ❗ Everything else secured
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}