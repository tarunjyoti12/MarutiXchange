package com.marutixchange.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        log.info("Incoming Request: {} {}", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {

            if (exchange.getResponse().getStatusCode() != null) {
                int status = exchange.getResponse().getStatusCode().value();
                log.info("Response Status: {}", status);
            } else {
                log.info("Response Status: UNKNOWN");
            }

        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}