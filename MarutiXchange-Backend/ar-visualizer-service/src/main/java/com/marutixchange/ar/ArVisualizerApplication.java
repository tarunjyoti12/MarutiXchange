package com.marutixchange.ar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ArVisualizerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArVisualizerApplication.class, args);
    }
}
