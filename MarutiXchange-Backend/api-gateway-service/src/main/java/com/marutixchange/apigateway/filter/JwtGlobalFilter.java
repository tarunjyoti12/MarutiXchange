package com.marutixchange.apigateway.filter;

import com.marutixchange.apigateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Public paths - no JWT needed
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/users/login",
            "/api/v1/users/register",
            "/api/v1/users/send-otp",
            "/api/v1/users/verify-otp",
            "/api/v1/users/forgot-password",
            "/api/v1/users/reset-password",
            "/api/v1/listings",          // public browse
            "/api/v1/search",            // public search
            "/api/v1/auctions",          // public auction view
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT check for public paths
        if (isPublicPath(path)) {
            log.debug("Public path - skipping JWT: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders().getFirst("Authorization");

        // No token provided
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            // Validate token
            jwtUtil.validateToken(token);

            // Extract claims
            String username = jwtUtil.extractUsername(token);
            String role     = jwtUtil.extractRoles(token);

            // Forward user info as headers to downstream services
            // Downstream services read X-User-Id instead of parsing JWT again
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User",    username != null ? username : "")
                    .header("X-Role",    role     != null ? role     : "")
                    .build();

            log.debug("JWT valid - user: {}, role: {}, path: {}", username, role, path);

            return chain.filter(
                    exchange.mutate().request(mutatedRequest).build()
            );

        } catch (Exception e) {
            log.warn("JWT validation failed for path {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -3; // Run before all other filters (order -1)
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
