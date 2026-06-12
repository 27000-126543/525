package com.ecommerce.platform.recommendation.service;

import java.util.List;
import java.util.Map;

public interface RecommendationService {

    List<Long> recommendForUser(Long userId, Integer limit);

    List<Long> getHotProducts(Integer limit);

    List<Long> getSimilarProducts(Long productId, Integer limit);

    List<Long> getNewProducts(Integer limit);

    List<Long> getRecommendByCategory(Long categoryId, Integer limit);

    void recordUserBehavior(Long userId, Long productId, String behaviorType, Integer score);

    void refreshHotProducts();

    void refreshUserRecommend(Long userId);

    Map<String, Object> getStats();
}
