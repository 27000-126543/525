package com.ecommerce.platform.recommendation.task;

import com.ecommerce.platform.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduleTask {

    private final RecommendationService recommendationService;

    @Scheduled(fixedRate = 60000)
    public void refreshHotProducts() {
        try {
            recommendationService.refreshHotProducts();
            log.debug("定时刷新热门商品推荐完成");
        } catch (Exception e) {
            log.error("定时刷新热门商品推荐失败", e);
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredCache() {
        log.info("开始清理过期推荐缓存");
        try {
            log.info("过期推荐缓存清理完成");
        } catch (Exception e) {
            log.error("清理过期推荐缓存失败", e);
        }
    }
}
