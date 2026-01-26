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
                        // INTERNAL
                        .requestMatchers("/api/internal/**").permitAll()

                        // ADMIN - specific paths
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")

                        // CUSTOMER
                        .requestMatchers("/api/customer/**").hasAnyRole("CUSTOMER", "ADMIN")
//                        .requestMatchers("/api/beneficiaries/**").hasAnyRole("CUSTOMER", "ADMIN")
//                        .requestMatchers("/api/transactions/**").hasAnyRole("CUSTOMER", "ADMIN")
                                .requestMatchers("/api/beneficiaries/**").hasRole("CUSTOMER")
                                .requestMatchers("/api/transactions/**").hasRole("CUSTOMER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}