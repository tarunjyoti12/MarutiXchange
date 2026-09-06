package com.marutixchange.apigateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        exchange.getRequest().mutate()
                .header(HEADER, correlationId)
                .build();

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}