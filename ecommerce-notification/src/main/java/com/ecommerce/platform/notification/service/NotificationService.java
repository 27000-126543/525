package com.ecommerce.platform.notification.service;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    boolean sendSms(String mobile, String templateCode, Map<String, String> params, Long tenantId);

    boolean sendEmail(String to, String subject, String content, String templateCode,
                      Map<String, String> params, Long tenantId);

    boolean sendInApp(Long userId, String title, String content, String type,
                      Map<String, Object> extra, Long tenantId);

    boolean sendAppPush(Long userId, String title, String content, String type,
                        Map<String, Object> extra, Long tenantId);

    boolean sendByChannel(String channel, String receiver, String title, String content,
                          String templateCode, Map<String, Object> params, Long tenantId, Integer priority);

    boolean sendBatch(String channel, List<String> receivers, String title, String content,
                      String templateCode, Map<String, Object> params, Long tenantId);

    boolean sendMultiChannel(List<String> channels, String receiver, String title, String content,
                             String templateCode, Map<String, Object> params, Long tenantId);

    Map<String, Object> getSendStatus(String messageId);

    List<Map<String, Object>> getMessageHistory(Long userId, String channel, Integer limit, Long tenantId);

    Map<String, Object> getChannelStats(String channel, Long tenantId);

    void retryFailedMessages();
}
