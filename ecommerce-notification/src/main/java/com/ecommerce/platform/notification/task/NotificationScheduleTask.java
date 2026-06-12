package com.ecommerce.platform.notification.task;

import com.ecommerce.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduleTask {

    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void retryFailedMessages() {
        try {
            notificationService.retryFailedMessages();
        } catch (Exception e) {
            log.error("重试失败消息任务异常", e);
        }
    }
}
