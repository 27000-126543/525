package com.ecommerce.platform.product.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.product.dto.StockDeductionDTO;
import com.ecommerce.platform.product.mapper.SeckillProductMapper;
import com.ecommerce.platform.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductionConsumer {

    private final StockService stockService;
    private final SeckillProductMapper seckillProductMapper;

    @StreamListener("stock-deduction-in")
    public void handleStockDeduction(Message<String> message) {
        try {
            String payload = message.getPayload();
            StockDeductionDTO dto = JSON.parseObject(payload, StockDeductionDTO.class);

            log.info("收到库存扣减消息, orderNo: {}", dto.getOrderNo());

            boolean success = stockService.deductStock(dto);
            if (success) {
                log.info("库存扣减成功, orderNo: {}", dto.getOrderNo());
            } else {
                log.error("库存扣减失败, orderNo: {}", dto.getOrderNo());
            }
        } catch (Exception e) {
            log.error("处理库存扣减消息异常", e);
            throw new RuntimeException("库存扣减消息处理失败", e);
        }
    }

    @StreamListener("stock-restore-in")
    public void handleStockRestore(Message<String> message) {
        try {
            String payload = message.getPayload();
            StockDeductionDTO dto = JSON.parseObject(payload, StockDeductionDTO.class);

            log.info("收到库存回补消息, orderNo: {}", dto.getOrderNo());

            boolean success = stockService.restoreStock(dto);
            if (success) {
                log.info("库存回补成功, orderNo: {}", dto.getOrderNo());
            } else {
                log.error("库存回补失败, orderNo: {}", dto.getOrderNo());
            }
        } catch (Exception e) {
            log.error("处理库存回补消息异常", e);
            throw new RuntimeException("库存回补消息处理失败", e);
        }
    }
}
