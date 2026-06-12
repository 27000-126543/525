package com.ecommerce.platform.order.mq;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final StreamBridge streamBridge;

    public void sendOrderCreateMessage(String orderNo, Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderNo", orderNo);
        payload.put("userId", userId);
        payload.put("type", "CREATE");
        payload.put("timestamp", System.currentTimeMillis());

        boolean result = streamBridge.send("order-create-out",
                MessageBuilder.withPayload(JSON.toJSONString(payload)).build());
        log.info("发送订单创建消息, orderNo: {}, result: {}", orderNo, result);
    }

    public void sendOrderCancelMessage(String orderNo, Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderNo", orderNo);
        payload.put("userId", userId);
        payload.put("type", "CANCEL");
        payload.put("timestamp", System.currentTimeMillis());

        boolean result = streamBridge.send("order-cancel-out",
                MessageBuilder.withPayload(JSON.toJSONString(payload)).build());
        log.info("发送订单取消消息, orderNo: {}, result: {}", orderNo, result);
    }
}
