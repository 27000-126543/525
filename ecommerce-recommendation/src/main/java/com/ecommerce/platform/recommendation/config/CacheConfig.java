package com.ecommerce.platform.recommendation.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .recordStats());
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "userRecommend",
                "hotProducts",
                "similarProducts",
                "newProducts",
                "categoryRecommend"
        ));
        return cacheManager;
    }

    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }
}
