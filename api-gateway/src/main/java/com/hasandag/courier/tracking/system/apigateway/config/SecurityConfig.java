package com.hasandag.courier.tracking.system.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Using NoOpPasswordEncoder for development to make debugging easier
        // WARNING: Never use this in production
        logger.info("Creating NoOpPasswordEncoder for development");
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        logger.info("Configuring security web filter chain");
        
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges

                        // Role-based endpoints
                        .pathMatchers("/user").hasRole("USER")
                        .pathMatchers("/admin").hasRole("ADMIN")
                        
                        // Actuator endpoints
                        .pathMatchers("/*/actuator/health/**").permitAll()
                        .pathMatchers("/actuator/health/**").permitAll()
                        .pathMatchers("/*/actuator/**").hasRole("ADMIN")
                        .pathMatchers("/actuator/**").hasRole("ADMIN")
                        
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                .httpBasic(httpBasic -> logger.info("Configuring HTTP Basic authentication"))
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
} 