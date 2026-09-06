package com.marutixchange.order_service.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    @Bean
    public KieContainer kieContainer() {
        return KieServices.Factory.get().getKieClasspathContainer();
    }

    // FIX: KieSession is NOT a bean anymore.
    // KieSession is stateful and NOT thread-safe — it must be created
    // fresh per request inside OrderServiceImpl and disposed after use.
    // Exposing it as a singleton Spring bean causes data mixing between
    // concurrent requests.
}
