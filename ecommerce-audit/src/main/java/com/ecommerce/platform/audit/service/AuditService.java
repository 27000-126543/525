package com.ecommerce.platform.audit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {

    void recordAuditLog(String module, String businessType, String operation, Long businessId,
                        String businessNo, Long operatorId, String operatorName,
                        Long orgId, Long tenantId, String ip, String userAgent,
                        String requestParams, String responseResult, Integer status,
                        Long costTime, String traceId);

    void recordTraceLog(String traceId, String spanId, String parentSpanId,
                        String serviceName, String methodName, String requestPath,
                        Long startTime, Long endTime, Integer status,
                        String requestParams, String responseResult,
                        String errorMessage, Map<String, Object> extra);

    Map<String, Object> searchAuditLogs(String module, String businessType, String operation,
                                         Long operatorId, Long tenantId, LocalDateTime startTime,
                                         LocalDateTime endTime, Integer page, Integer size);

    Map<String, Object> searchTraceLogs(String traceId, String serviceName, String methodName,
                                         LocalDateTime startTime, LocalDateTime endTime,
                                         Integer page, Integer size);

    List<Map<String, Object>> getAuditStats(String module, Long tenantId, String timeRange);

    List<Map<String, Object>> getTraceStats(String timeRange);

    Map<String, Object> getTraceDetail(String traceId);

    byte[] exportAuditLogs(String module, String businessType, Long tenantId,
                           LocalDateTime startTime, LocalDateTime endTime);

    void cleanExpiredLogs();
}
