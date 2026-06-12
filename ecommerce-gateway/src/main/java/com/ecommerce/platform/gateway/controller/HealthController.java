package com.ecommerce.platform.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "ecommerce-gateway");
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        return Mono.just(data);
    }
}
