package com.ecommerce.platform.analytics.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @StreamListener("order-event-in")
    public void handleOrderEvent(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);
            analyticsService.recordOrderEvent(data);
        } catch (Exception e) {
            log.error("处理订单事件消息异常", e);
        }
    }

    @StreamListener("product-event-in")
    public void handleProductEvent(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);
            analyticsService.recordProductEvent(data);
        } catch (Exception e) {
            log.error("处理商品事件消息异常", e);
        }
    }

    @StreamListener("user-behavior-in")
    public void handleUserBehavior(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);
            analyticsService.recordUserBehavior(data);
        } catch (Exception e) {
            log.error("处理用户行为消息异常", e);
        }
    }
}
