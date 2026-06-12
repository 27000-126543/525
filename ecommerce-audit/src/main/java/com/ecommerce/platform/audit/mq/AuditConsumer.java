package com.ecommerce.platform.audit.mq;

import com.alibaba.fastjson2.JSON;
import com.ecommerce.platform.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditConsumer {

    private final AuditService auditService;

    @StreamListener("audit-log-in")
    public void handleAuditLog(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);

            String module = (String) data.get("module");
            String businessType = (String) data.get("businessType");
            String operation = (String) data.get("operation");
            Long businessId = data.get("businessId") != null
                    ? Long.valueOf(data.get("businessId").toString())
                    : null;
            String businessNo = (String) data.get("businessNo");
            Long operatorId = data.get("operatorId") != null
                    ? Long.valueOf(data.get("operatorId").toString())
                    : null;
            String operatorName = (String) data.get("operatorName");
            Long orgId = data.get("orgId") != null
                    ? Long.valueOf(data.get("orgId").toString())
                    : null;
            Long tenantId = data.get("tenantId") != null
                    ? Long.valueOf(data.get("tenantId").toString())
                    : null;
            String ip = (String) data.get("ip");
            String userAgent = (String) data.get("userAgent");
            String requestParams = data.get("requestParams") != null
                    ? data.get("requestParams").toString()
                    : null;
            String responseResult = data.get("responseResult") != null
                    ? data.get("responseResult").toString()
                    : null;
            Integer status = data.get("status") != null
                    ? Integer.valueOf(data.get("status").toString())
                    : 0;
            Long costTime = data.get("costTime") != null
                    ? Long.valueOf(data.get("costTime").toString())
                    : 0L;
            String traceId = (String) data.get("traceId");

            auditService.recordAuditLog(module, businessType, operation, businessId,
                    businessNo, operatorId, operatorName, orgId, tenantId,
                    ip, userAgent, requestParams, responseResult, status, costTime, traceId);

        } catch (Exception e) {
            log.error("处理审计日志消息异常", e);
        }
    }

    @StreamListener("trace-log-in")
    public void handleTraceLog(Message<String> message) {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = JSON.parseObject(payload, Map.class);

            String traceId = (String) data.get("traceId");
            String spanId = (String) data.get("spanId");
            String parentSpanId = (String) data.get("parentSpanId");
            String serviceName = (String) data.get("serviceName");
            String methodName = (String) data.get("methodName");
            String requestPath = (String) data.get("requestPath");
            Long startTime = data.get("startTime") != null
                    ? Long.valueOf(data.get("startTime").toString())
                    : System.currentTimeMillis();
            Long endTime = data.get("endTime") != null
                    ? Long.valueOf(data.get("endTime").toString())
                    : System.currentTimeMillis();
            Integer status = data.get("status") != null
                    ? Integer.valueOf(data.get("status").toString())
                    : 0;
            String requestParams = data.get("requestParams") != null
                    ? data.get("requestParams").toString()
                    : null;
            String responseResult = data.get("responseResult") != null
                    ? data.get("responseResult").toString()
                    : null;
            String errorMessage = (String) data.get("errorMessage");

            @SuppressWarnings("unchecked")
            Map<String, Object> extra = (Map<String, Object>) data.get("extra");

            auditService.recordTraceLog(traceId, spanId, parentSpanId,
                    serviceName, methodName, requestPath,
                    startTime, endTime, status,
                    requestParams, responseResult, errorMessage, extra);

        } catch (Exception e) {
            log.error("处理链路日志消息异常", e);
        }
    }
}
