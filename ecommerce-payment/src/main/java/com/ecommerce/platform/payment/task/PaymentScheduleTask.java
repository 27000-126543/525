package com.ecommerce.platform.payment.task;

import com.ecommerce.platform.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduleTask {

    private final PaymentService paymentService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void reconciliationTask() {
        log.info("开始自动对账任务");
        try {
            paymentService.doReconciliation();
            log.info("自动对账任务完成");
        } catch (Exception e) {
            log.error("自动对账任务失败", e);
        }
    }
}
