package com.ecommerce.platform.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${gateway.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${gateway.rate-limit.default-limit-per-second:100}")
    private int defaultLimitPerSecond;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(exchange);
        String path = exchange.getRequest().getURI().getPath();

        String rateLimitKey = "gateway:ratelimit:" + path + ":" + clientIp;

        return redisTemplate.opsForValue().increment(rateLimitKey)
                .flatMap(current -> {
                    if (current == 1) {
                        return redisTemplate.expire(rateLimitKey, Duration.ofSeconds(1))
                                .thenReturn(current);
                    }
                    return Mono.just(current);
                })
                .flatMap(current -> {
                    if (current > defaultLimitPerSecond) {
                        log.warn("请求被限流, ip: {}, path: {}, count: {}", clientIp, path, current);
                        return rateLimitResponse(exchange);
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("限流处理异常", e);
                    return chain.filter(exchange);
                });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() && exchange.getRequest().getRemoteAddress() != null) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    private Mono<Void> rateLimitResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.fail(ResultCode.RATE_LIMIT_EXCEEDED);
        String json = JSON.toJSONString(result);

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
