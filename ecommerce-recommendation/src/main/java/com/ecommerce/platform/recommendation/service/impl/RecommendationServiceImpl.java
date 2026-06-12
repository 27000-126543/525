package com.ecommerce.platform.recommendation.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.ecommerce.platform.common.constant.RedisConstants;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RedisUtil redisUtil;
    private final Cache<String, Object> localCache;

    @Value("${recommendation.recommend.default-size:10}")
    private Integer defaultSize;

    @Value("${recommendation.recommend.max-size:50}")
    private Integer maxSize;

    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong cacheHitCount = new AtomicLong(0);

    @Override
    public List<Long> recommendForUser(Long userId, Integer limit) {
        requestCount.incrementAndGet();

        if (limit == null || limit < 1) {
            limit = defaultSize;
        }
        limit = Math.min(limit, maxSize);

        String localKey = "user:rec:" + userId;
        @SuppressWarnings("unchecked")
        List<Long> localResult = (List<Long>) localCache.getIfPresent(localKey);
        if (localResult != null && localResult.size() >= limit) {
            cacheHitCount.incrementAndGet();
            return localResult.subList(0, Math.min(limit, localResult.size()));
        }

        String redisKey = RedisConstants.RECOMMEND_USER_PREFIX + userId;
        Set<Object> redisResult = redisUtil.zReverseRange(redisKey, 0, limit - 1);
        if (redisResult != null && !redisResult.isEmpty()) {
            cacheHitCount.incrementAndGet();
            List<Long> result = new ArrayList<>();
            for (Object obj : redisResult) {
                result.add(Long.valueOf(obj.toString()));
            }
            localCache.put(localKey, result);
            return result.subList(0, Math.min(limit, result.size()));
        }

        return getFallbackRecommendations(limit);
    }

    @Override
    public List<Long> getHotProducts(Integer limit) {
        requestCount.incrementAndGet();

        if (limit == null || limit < 1) {
            limit = defaultSize;
        }
        limit = Math.min(limit, maxSize);

        String localKey = "hot:products";
        @SuppressWarnings("unchecked")
        List<Long> localResult = (List<Long>) localCache.getIfPresent(localKey);
        if (localResult != null && localResult.size() >= limit) {
            cacheHitCount.incrementAndGet();
            return localResult.subList(0, Math.min(limit, localResult.size()));
        }

        String redisKey = RedisConstants.RECOMMEND_HOT_PREFIX;
        Set<Object> redisResult = redisUtil.zReverseRange(redisKey, 0, limit - 1);
        if (redisResult != null && !redisResult.isEmpty()) {
            cacheHitCount.incrementAndGet();
            List<Long> result = new ArrayList<>();
            for (Object obj : redisResult) {
                result.add(Long.valueOf(obj.toString()));
            }
            localCache.put(localKey, result);
            return result.subList(0, Math.min(limit, result.size()));
        }

        return getFallbackRecommendations(limit);
    }

    @Override
    public List<Long> getSimilarProducts(Long productId, Integer limit) {
        requestCount.incrementAndGet();

        if (limit == null || limit < 1) {
            limit = defaultSize;
        }
        limit = Math.min(limit, maxSize);

        String localKey = "similar:" + productId;
        @SuppressWarnings("unchecked")
        List<Long> localResult = (List<Long>) localCache.getIfPresent(localKey);
        if (localResult != null && localResult.size() >= limit) {
            cacheHitCount.incrementAndGet();
            return localResult.subList(0, Math.min(limit, localResult.size()));
        }

        String redisKey = "recommend:similar:" + productId;
        Set<Object> redisResult = redisUtil.zReverseRange(redisKey, 0, limit - 1);
        if (redisResult != null && !redisResult.isEmpty()) {
            cacheHitCount.incrementAndGet();
            List<Long> result = new ArrayList<>();
            for (Object obj : redisResult) {
                result.add(Long.valueOf(obj.toString()));
            }
            localCache.put(localKey, result);
            return result.subList(0, Math.min(limit, result.size()));
        }

        return getFallbackRecommendations(limit);
    }

    @Override
    public List<Long> getNewProducts(Integer limit) {
        requestCount.incrementAndGet();

        if (limit == null || limit < 1) {
            limit = defaultSize;
        }
        limit = Math.min(limit, maxSize);

        String localKey = "new:products";
        @SuppressWarnings("unchecked")
        List<Long> localResult = (List<Long>) localCache.getIfPresent(localKey);
        if (localResult != null && localResult.size() >= limit) {
            cacheHitCount.incrementAndGet();
            return localResult.subList(0, Math.min(limit, localResult.size()));
        }

        String redisKey = "recommend:new";
        List<Object> redisResult = redisUtil.lRange(redisKey, 0, limit - 1);
        if (redisResult != null && !redisResult.isEmpty()) {
            cacheHitCount.incrementAndGet();
            List<Long> result = new ArrayList<>();
            for (Object obj : redisResult) {
                result.add(Long.valueOf(obj.toString()));
            }
            localCache.put(localKey, result);
            return result.subList(0, Math.min(limit, result.size()));
        }

        return getFallbackRecommendations(limit);
    }

    @Override
    public List<Long> getRecommendByCategory(Long categoryId, Integer limit) {
        requestCount.incrementAndGet();

        if (limit == null || limit < 1) {
            limit = defaultSize;
        }
        limit = Math.min(limit, maxSize);

        String localKey = "category:rec:" + categoryId;
        @SuppressWarnings("unchecked")
        List<Long> localResult = (List<Long>) localCache.getIfPresent(localKey);
        if (localResult != null && localResult.size() >= limit) {
            cacheHitCount.incrementAndGet();
            return localResult.subList(0, Math.min(limit, localResult.size()));
        }

        String redisKey = "recommend:category:" + categoryId;
        Set<Object> redisResult = redisUtil.zReverseRange(redisKey, 0, limit - 1);
        if (redisResult != null && !redisResult.isEmpty()) {
            cacheHitCount.incrementAndGet();
            List<Long> result = new ArrayList<>();
            for (Object obj : redisResult) {
                result.add(Long.valueOf(obj.toString()));
            }
            localCache.put(localKey, result);
            return result.subList(0, Math.min(limit, result.size()));
        }

        return getFallbackRecommendations(limit);
    }

    @Override
    public void recordUserBehavior(Long userId, Long productId, String behaviorType, Integer score) {
        if (score == null || score < 0) {
            score = getDefaultScore(behaviorType);
        }

        String key = RedisConstants.RECOMMEND_USER_PREFIX + userId;
        redisUtil.zIncrementScore(key, String.valueOf(productId), score);
        redisUtil.expire(key, 48, TimeUnit.HOURS);

        String hotKey = RedisConstants.RECOMMEND_HOT_PREFIX;
        redisUtil.zIncrementScore(hotKey, String.valueOf(productId), score * 0.1);
    }

    @Override
    public void refreshHotProducts() {
        log.info("开始刷新热门商品推荐");
        String hotKey = RedisConstants.RECOMMEND_HOT_PREFIX;

        try {
            Set<Object> currentHot = redisUtil.zReverseRange(hotKey, 0, 999);
            if (currentHot != null && !currentHot.isEmpty()) {
                List<Long> hotList = new ArrayList<>();
                for (Object obj : currentHot) {
                    hotList.add(Long.valueOf(obj.toString()));
                }
                localCache.put("hot:products", hotList);
            }
            log.info("热门商品推荐刷新完成, 数量: {}", currentHot != null ? currentHot.size() : 0);
        } catch (Exception e) {
            log.error("刷新热门商品推荐失败", e);
        }
    }

    @Override
    public void refreshUserRecommend(Long userId) {
        log.debug("刷新用户推荐, userId: {}", userId);
        String localKey = "user:rec:" + userId;
        localCache.invalidate(localKey);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long total = requestCount.get();
        long hits = cacheHitCount.get();
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;

        stats.put("totalRequests", total);
        stats.put("cacheHits", hits);
        stats.put("cacheHitRate", String.format("%.2f%%", hitRate));
        stats.put("localCacheSize", localCache.estimatedSize());
        stats.put("localCacheStats", localCache.stats().toString());

        return stats;
    }

    private Integer getDefaultScore(String behaviorType) {
        if (behaviorType == null) {
            return 1;
        }
        return switch (behaviorType.toLowerCase()) {
            case "view" -> 1;
            case "collect" -> 5;
            case "cart" -> 10;
            case "buy" -> 50;
            case "comment" -> 5;
            case "share" -> 3;
            default -> 1;
        };
    }

    private List<Long> getFallbackRecommendations(Integer limit) {
        List<Long> fallback = new ArrayList<>();
        for (long i = 1; i <= Math.min(limit, 20); i++) {
            fallback.add(i * 10000L);
        }
        return fallback.subList(0, Math.min(limit, fallback.size()));
    }
}
