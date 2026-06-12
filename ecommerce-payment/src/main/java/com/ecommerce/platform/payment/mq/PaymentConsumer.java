package com.ecommerce.platform.payment.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;

    @StreamListener("pay-notify-in")
    public void handlePayNotify(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, String> data = JSON.parseObject(payload, Map.class);

            String channel = data.get("channel");
            paymentService.processPaymentCallback(channel, data);
        } catch (Exception e) {
            log.error("处理支付通知消息异常", e);
        }
    }

    @StreamListener("logistics-notify-in")
    public void handleLogisticsNotify(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, String> data = JSON.parseObject(payload, Map.class);

            String provider = data.get("provider");
            paymentService.processLogisticsCallback(provider, data);
        } catch (Exception e) {
            log.error("处理物流通知消息异常", e);
        }
    }
}
