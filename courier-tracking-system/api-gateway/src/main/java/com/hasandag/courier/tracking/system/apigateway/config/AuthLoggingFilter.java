package com.hasandag.courier.tracking.system.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Component
public class AuthLoggingFilter implements WebFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        logger.info("Request received for path: {}", path);
        
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String credentials = new String(Base64.getDecoder().decode(authHeader.substring(6)));
                String[] userAndPassword = credentials.split(":", 2);
                
                if (userAndPassword.length == 2) {
                    logger.info("Authentication attempt with username: {}", userAndPassword[0]);
                } else {
                    logger.warn("Malformed basic auth credentials");
                }
            } catch (Exception e) {
                logger.error("Error decoding Basic Auth header", e);
            }
        } else {
            logger.info("No Authorization header present");
        }
        
        return chain.filter(exchange);
    }
} 