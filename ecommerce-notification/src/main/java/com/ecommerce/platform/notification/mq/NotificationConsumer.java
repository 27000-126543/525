package com.ecommerce.platform.notification.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @StreamListener("notification-in")
    public void handleNotification(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);

            String channel = (String) data.get("channel");
            String receiver = (String) data.get("receiver");
            String title = (String) data.get("title");
            String content = (String) data.get("content");
            String templateCode = (String) data.get("templateCode");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) data.get("params");
            Long tenantId = data.get("tenantId") != null
                    ? Long.valueOf(data.get("tenantId").toString())
                    : null;
            Integer priority = data.get("priority") != null
                    ? Integer.valueOf(data.get("priority").toString())
                    : 2;

            notificationService.sendByChannel(channel, receiver, title, content,
                    templateCode, params, tenantId, priority);

        } catch (Exception e) {
            log.error("处理通知消息异常", e);
        }
    }
}
