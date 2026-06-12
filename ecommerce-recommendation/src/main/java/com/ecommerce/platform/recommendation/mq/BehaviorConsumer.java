package com.ecommerce.platform.recommendation.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BehaviorConsumer {

    private final RecommendationService recommendationService;

    @StreamListener("user-behavior-in")
    public void handleUserBehavior(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);

            Long userId = Long.valueOf(data.get("userId").toString());
            Long productId = Long.valueOf(data.get("productId").toString());
            String behaviorType = (String) data.get("behaviorType");
            Integer score = data.get("score") != null
                    ? Integer.valueOf(data.get("score").toString())
                    : null;

            recommendationService.recordUserBehavior(userId, productId, behaviorType, score);
        } catch (Exception e) {
            log.error("处理用户行为消息异常", e);
        }
    }

    @StreamListener("hot-product-in")
    public void handleHotProductUpdate(Message<String> message) {
        try {
            recommendationService.refreshHotProducts();
            log.info("热门商品推荐已刷新");
        } catch (Exception e) {
            log.error("处理热门商品更新消息异常", e);
        }
    }
}
