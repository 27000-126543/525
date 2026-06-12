package com.ecommerce.platform.gateway.filter;

import cn.hutool.core.util.StrUtil;
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
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${gateway.auth.exclude-paths:}")
    private List<String> excludePaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isExcludePath(path)) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("Authorization");
        if (StrUtil.isBlank(token)) {
            token = request.getQueryParams().getFirst("token");
        }

        if (StrUtil.isBlank(token)) {
            return unauthorizedResponse(exchange, "未授权");
        }

        String tokenKey = "auth:token:" + token;
        return redisTemplate.opsForValue().get(tokenKey)
                .flatMap(userInfo -> {
                    if (StrUtil.isBlank(userInfo)) {
                        return unauthorizedResponse(exchange, "Token已过期或无效");
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(unauthorizedResponse(exchange, "Token已过期或无效"))
                .onErrorResume(e -> {
                    log.error("认证异常", e);
                    return unauthorizedResponse(exchange, "认证失败");
                });
    }

    private boolean isExcludePath(String path) {
        if (excludePaths == null || excludePaths.isEmpty()) {
            return false;
        }
        for (String excludePath : excludePaths) {
            if (path.startsWith(excludePath.replace("/**", ""))) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED.getCode(), message);
        String json = JSON.toJSONString(result);

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
