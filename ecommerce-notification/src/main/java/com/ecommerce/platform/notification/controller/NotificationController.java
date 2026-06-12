package com.ecommerce.platform.notification.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "消息推送")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "发送短信")
    @PostMapping("/sms/send")
    public Result<Boolean> sendSms(
            @RequestParam String mobile,
            @RequestParam String templateCode,
            @RequestParam(required = false) Map<String, String> params) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.sendSms(mobile, templateCode, params, tenantId));
    }

    @Operation(summary = "发送邮件")
    @PostMapping("/email/send")
    public Result<Boolean> sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) Map<String, String> params) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.sendEmail(to, subject, content, templateCode, params, tenantId));
    }

    @Operation(summary = "发送站内信")
    @PostMapping("/inapp/send")
    public Result<Boolean> sendInApp(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "system") String type,
            @RequestParam(required = false) Map<String, Object> extra) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.sendInApp(userId, title, content, type, extra, tenantId));
    }

    @Operation(summary = "发送App推送")
    @PostMapping("/apppush/send")
    public Result<Boolean> sendAppPush(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "system") String type,
            @RequestParam(required = false) Map<String, Object> extra) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.sendAppPush(userId, title, content, type, extra, tenantId));
    }

    @Operation(summary = "按渠道发送消息")
    @PostMapping("/send")
    public Result<Boolean> sendByChannel(
            @RequestParam String channel,
            @RequestParam String receiver,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) Map<String, Object> params,
            @RequestParam(defaultValue = "2") Integer priority) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.sendByChannel(
                channel, receiver, title, content, templateCode, params, tenantId, priority));
    }

    @Operation(summary = "批量发送消息")
    @PostMapping("/batch/send")
    public Result<Boolean> sendBatch(
            @RequestParam String channel,
            @RequestParam List<String> receivers,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) Map<String, Object> params) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.sendBatch(
                channel, receivers, title, content, templateCode, params, tenantId));
    }

    @Operation(summary = "多渠道发送")
    @PostMapping("/multiChannel/send")
    public Result<Boolean> sendMultiChannel(
            @RequestParam List<String> channels,
            @RequestParam String receiver,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) Map<String, Object> params) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.sendMultiChannel(
                channels, receiver, title, content, templateCode, params, tenantId));
    }

    @Operation(summary = "查询发送状态")
    @GetMapping("/status/{messageId}")
    public Result<Map<String, Object>> getSendStatus(@PathVariable String messageId) {
        return Result.success(notificationService.getSendStatus(messageId));
    }

    @Operation(summary = "我的消息列表")
    @GetMapping("/my/list")
    public Result<List<Map<String, Object>>> getMyMessages(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = UserContext.getUserId();
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.getMessageHistory(userId, channel, limit, tenantId));
    }

    @Operation(summary = "渠道统计")
    @GetMapping("/stats/{channel}")
    public Result<Map<String, Object>> getChannelStats(@PathVariable String channel) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(notificationService.getChannelStats(channel, tenantId));
    }

    @Operation(summary = "重试失败消息")
    @PostMapping("/retry")
    public Result<Void> retryFailedMessages() {
        notificationService.retryFailedMessages();
        return Result.success();
    }
}
