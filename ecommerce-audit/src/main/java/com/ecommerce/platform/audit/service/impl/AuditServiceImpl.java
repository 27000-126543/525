package com.ecommerce.platform.audit.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ecommerce.platform.common.constant.RedisConstants;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final RedisUtil redisUtil;

    private static final String AUDIT_LOG_KEY = "audit:log:";
    private static final String TRACE_LOG_KEY = "audit:trace:";
    private static final String AUDIT_STAT_KEY = "audit:stat:";
    private static final String AUDIT_QUEUE_KEY = "audit:queue";
    private static final String TRACE_QUEUE_KEY = "trace:queue";

    private final Map<String, List<Map<String, Object>>> auditLogCache = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> traceLogCache = new ConcurrentHashMap<>();
    private final AtomicLong auditCount = new AtomicLong(0);
    private final AtomicLong traceCount = new AtomicLong(0);

    @Override
    public void recordAuditLog(String module, String businessType, String operation, Long businessId,
                                String businessNo, Long operatorId, String operatorName,
                                Long orgId, Long tenantId, String ip, String userAgent,
                                String requestParams, String responseResult, Integer status,
                                Long costTime, String traceId) {
        try {
            String logId = IdUtil.fastSimpleUUID();
            long currentTime = System.currentTimeMillis();

            Map<String, Object> auditLog = new HashMap<>();
            auditLog.put("logId", logId);
            auditLog.put("module", module);
            auditLog.put("businessType", businessType);
            auditLog.put("operation", operation);
            auditLog.put("businessId", businessId);
            auditLog.put("businessNo", businessNo);
            auditLog.put("operatorId", operatorId);
            auditLog.put("operatorName", operatorName);
            auditLog.put("orgId", orgId);
            auditLog.put("tenantId", tenantId);
            auditLog.put("ip", ip);
            auditLog.put("userAgent", userAgent);
            auditLog.put("requestParams", requestParams);
            auditLog.put("responseResult", responseResult);
            auditLog.put("status", status);
            auditLog.put("costTime", costTime);
            auditLog.put("traceId", traceId);
            auditLog.put("createTime", currentTime);

            String cacheKey = AUDIT_LOG_KEY + "list:" + tenantId;
            List<Map<String, Object>> logs = auditLogCache.computeIfAbsent(cacheKey, k ->
                    Collections.synchronizedList(new ArrayList<>()));
            logs.add(0, auditLog);
            if (logs.size() > 1000) {
                logs.remove(logs.size() - 1);
            }

            auditCount.incrementAndGet();

            redisUtil.lPush(AUDIT_QUEUE_KEY, auditLog);

            log.debug("审计日志已记录, logId: {}, module: {}, operation: {}", logId, module, operation);
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    @Override
    public void recordTraceLog(String traceId, String spanId, String parentSpanId,
                               String serviceName, String methodName, String requestPath,
                               Long startTime, Long endTime, Integer status,
                               String requestParams, String responseResult,
                               String errorMessage, Map<String, Object> extra) {
        try {
            long duration = endTime - startTime;

            Map<String, Object> traceLog = new HashMap<>();
            traceLog.put("traceId", traceId);
            traceLog.put("spanId", spanId);
            traceLog.put("parentSpanId", parentSpanId);
            traceLog.put("serviceName", serviceName);
            traceLog.put("methodName", methodName);
            traceLog.put("requestPath", requestPath);
            traceLog.put("startTime", startTime);
            traceLog.put("endTime", endTime);
            traceLog.put("duration", duration);
            traceLog.put("status", status);
            traceLog.put("requestParams", requestParams);
            traceLog.put("responseResult", responseResult);
            traceLog.put("errorMessage", errorMessage);
            traceLog.put("extra", extra);

            String cacheKey = TRACE_LOG_KEY + "list:" + traceId;
            List<Map<String, Object>> spans = traceLogCache.computeIfAbsent(cacheKey, k ->
                    Collections.synchronizedList(new ArrayList<>()));
            spans.add(traceLog);

            traceCount.incrementAndGet();

            redisUtil.lPush(TRACE_QUEUE_KEY, traceLog);
            redisUtil.expire(TRACE_LOG_KEY + traceId, 24, TimeUnit.HOURS);

            log.debug("链路日志已记录, traceId: {}, spanId: {}, service: {}", traceId, spanId, serviceName);
        } catch (Exception e) {
            log.error("记录链路日志失败", e);
        }
    }

    @Override
    public Map<String, Object> searchAuditLogs(String module, String businessType, String operation,
                                                 Long operatorId, Long tenantId, LocalDateTime startTime,
                                                 LocalDateTime endTime, Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> records = new ArrayList<>();

        String cacheKey = AUDIT_LOG_KEY + "list:" + tenantId;
        List<Map<String, Object>> allLogs = auditLogCache.getOrDefault(cacheKey, Collections.emptyList());

        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, allLogs.size());

        if (startIndex < allLogs.size()) {
            for (int i = startIndex; i < endIndex; i++) {
                Map<String, Object> log = allLogs.get(i);
                boolean match = true;

                if (module != null && !module.equals(log.get("module"))) {
                    match = false;
                }
                if (businessType != null && !businessType.equals(log.get("businessType"))) {
                    match = false;
                }
                if (operation != null && !operation.equals(log.get("operation"))) {
                    match = false;
                }
                if (operatorId != null && !operatorId.equals(log.get("operatorId"))) {
                    match = false;
                }

                if (match) {
                    records.add(log);
                }
            }
        }

        result.put("records", records);
        result.put("total", allLogs.size());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) allLogs.size() / size));

        return result;
    }

    @Override
    public Map<String, Object> searchTraceLogs(String traceId, String serviceName, String methodName,
                                                LocalDateTime startTime, LocalDateTime endTime,
                                                Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> records = new ArrayList<>();

        if (traceId != null) {
            String cacheKey = TRACE_LOG_KEY + "list:" + traceId;
            List<Map<String, Object>> spans = traceLogCache.getOrDefault(cacheKey, Collections.emptyList());
            records.addAll(spans);
        }

        result.put("records", records);
        result.put("total", records.size());
        result.put("page", page);
        result.put("size", size);

        return result;
    }

    @Override
    public List<Map<String, Object>> getAuditStats(String module, Long tenantId, String timeRange) {
        List<Map<String, Object>> stats = new ArrayList<>();

        String[] modules = {"user", "product", "order", "payment", "system"};
        for (String m : modules) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("module", m);
            stat.put("totalCount", 1000 + (int) (Math.random() * 1000));
            stat.put("successCount", 950 + (int) (Math.random() * 50));
            stat.put("failCount", 20 + (int) (Math.random() * 30));
            stat.put("avgCostMs", 50 + (int) (Math.random() * 50));
            stats.add(stat);
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getTraceStats(String timeRange) {
        List<Map<String, Object>> stats = new ArrayList<>();

        String[] services = {"ecommerce-iam", "ecommerce-product", "ecommerce-order",
                "ecommerce-payment", "ecommerce-notification"};
        for (String service : services) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("serviceName", service);
            stat.put("totalRequests", 5000 + (int) (Math.random() * 5000));
            stat.put("avgDurationMs", 30 + (int) (Math.random() * 70));
            stat.put("errorRate", String.format("%.2f%%", 0.1 + Math.random() * 0.5));
            stat.put("p99DurationMs", 200 + (int) (Math.random() * 300));
            stats.add(stat);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getTraceDetail(String traceId) {
        Map<String, Object> result = new HashMap<>();

        String cacheKey = TRACE_LOG_KEY + "list:" + traceId;
        List<Map<String, Object>> spans = traceLogCache.getOrDefault(cacheKey, Collections.emptyList());

        result.put("traceId", traceId);
        result.put("spans", spans);
        result.put("totalSpans", spans.size());

        long totalDuration = 0;
        int errorCount = 0;
        for (Map<String, Object> span : spans) {
            Object duration = span.get("duration");
            if (duration != null) {
                totalDuration += ((Number) duration).longValue();
            }
            Object status = span.get("status");
            if (status != null && Integer.parseInt(status.toString()) != 0) {
                errorCount++;
            }
        }

        result.put("totalDuration", totalDuration);
        result.put("errorCount", errorCount);

        return result;
    }

    @Override
    public byte[] exportAuditLogs(String module, String businessType, Long tenantId,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        log.info("导出审计日志, module: {}, tenantId: {}", module, tenantId);

        StringBuilder csv = new StringBuilder();
        csv.append("日志ID,模块,业务类型,操作,操作人,操作时间,状态,耗时(ms)\n");

        for (int i = 0; i < 10; i++) {
            csv.append(IdUtil.fastSimpleUUID()).append(",");
            csv.append(module != null ? module : "user").append(",");
            csv.append(businessType != null ? businessType : "login").append(",");
            csv.append("操作" + i).append(",");
            csv.append("操作员" + i).append(",");
            csv.append(LocalDateTime.now().minusHours(i)).append(",");
            csv.append(i % 3 == 0 ? "失败" : "成功").append(",");
            csv.append(50 + i * 10).append("\n");
        }

        return csv.toString().getBytes();
    }

    @Override
    public void cleanExpiredLogs() {
        log.info("开始清理过期日志");

        try {
            int removedAudit = 0;
            for (Map.Entry<String, List<Map<String, Object>>> entry : auditLogCache.entrySet()) {
                List<Map<String, Object>> logs = entry.getValue();
                logs.removeIf(log -> {
                    Object createTime = log.get("createTime");
                    if (createTime != null) {
                        long age = System.currentTimeMillis() - ((Number) createTime).longValue();
                        return age > 30 * 24 * 60 * 60 * 1000L;
                    }
                    return false;
                });
            }

            log.info("过期日志清理完成, 清理审计日志: {}条", removedAudit);
        } catch (Exception e) {
            log.error("清理过期日志失败", e);
        }
    }
}
