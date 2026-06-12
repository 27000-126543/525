package com.ecommerce.platform.product.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.product.dto.StockDeductionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductionProducer {

    private final StreamBridge streamBridge;

    public void sendDeductionMessage(StockDeductionDTO dto) {
        String json = JSON.toJSONString(dto);
        boolean result = streamBridge.send("stock-deduction-out",
                MessageBuilder.withPayload(json).build());
        log.info("发送库存扣减消息, orderNo: {}, result: {}", dto.getOrderNo(), result);
    }

    public void sendRestoreMessage(StockDeductionDTO dto) {
        String json = JSON.toJSONString(dto);
        boolean result = streamBridge.send("stock-restore-out",
                MessageBuilder.withPayload(json).build());
        log.info("发送库存回补消息, orderNo: {}, result: {}", dto.getOrderNo(), result);
    }
}
