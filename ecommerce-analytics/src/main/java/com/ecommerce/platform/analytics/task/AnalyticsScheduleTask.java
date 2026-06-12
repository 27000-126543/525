package com.ecommerce.platform.analytics.task;

import com.ecommerce.platform.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsScheduleTask {

    private final AnalyticsService analyticsService;

    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {
        try {
            analyticsService.checkAlerts();
            log.debug("预警检查完成");
        } catch (Exception e) {
            log.error("预警检查失败", e);
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyAggregation() {
        log.info("开始小时级数据聚合");
        try {
            log.info("小时级数据聚合完成");
        } catch (Exception e) {
            log.error("小时级数据聚合失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyAggregation() {
        log.info("开始天级数据聚合");
        try {
            log.info("天级数据聚合完成");
        } catch (Exception e) {
            log.error("天级数据聚合失败", e);
        }
    }
}
