package com.ecommerce.platform.audit.task;

import com.ecommerce.platform.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditScheduleTask {

    private final AuditService auditService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredLogs() {
        log.info("开始执行日志清理定时任务");
        try {
            auditService.cleanExpiredLogs();
            log.info("日志清理定时任务完成");
        } catch (Exception e) {
            log.error("日志清理定时任务失败", e);
        }
    }
}
