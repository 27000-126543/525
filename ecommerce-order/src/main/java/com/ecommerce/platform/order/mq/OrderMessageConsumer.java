package com.ecommerce.platform.order.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageConsumer {

    private final OrderService orderService;

    @StreamListener("order-pay-success-in")
    public void handlePaySuccess(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);

            String orderNo = (String) data.get("orderNo");
            String transactionId = (String) data.get("transactionId");

            log.info("收到支付成功消息, orderNo: {}", orderNo);

            boolean success = orderService.paySuccess(orderNo, transactionId);
            if (success) {
                log.info("订单支付状态更新成功, orderNo: {}", orderNo);
            } else {
                log.error("订单支付状态更新失败, orderNo: {}", orderNo);
            }
        } catch (Exception e) {
            log.error("处理支付成功消息异常", e);
            throw new RuntimeException("支付成功消息处理失败", e);
        }
    }
}
