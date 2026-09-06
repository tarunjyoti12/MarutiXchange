package com.marutixchange.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // 🔥 FINAL FIX: allow ALL user APIs (login + register)
            if (path.startsWith("/api/users")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            // ❌ No token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                // ✅ Validate token
                jwtUtil.validateToken(token);

                // ✅ Extract user info
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRoles(token);

                // ✅ Add headers for downstream services
                exchange.getRequest().mutate()
                        .header("X-User", username)
                        .header("X-Role", role)
                        .build();

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}