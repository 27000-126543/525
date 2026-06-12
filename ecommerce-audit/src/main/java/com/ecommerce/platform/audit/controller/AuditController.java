package com.ecommerce.platform.audit.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "审计日志")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "查询审计日志")
    @GetMapping("/audit/search")
    public Result<Map<String, Object>> searchAuditLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(auditService.searchAuditLogs(
                module, businessType, operation, operatorId, tenantId,
                startTime, endTime, page, size));
    }

    @Operation(summary = "查询链路日志")
    @GetMapping("/trace/search")
    public Result<Map<String, Object>> searchTraceLogs(
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String methodName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(auditService.searchTraceLogs(
                traceId, serviceName, methodName, startTime, endTime, page, size));
    }

    @Operation(summary = "获取链路详情")
    @GetMapping("/trace/{traceId}")
    public Result<Map<String, Object>> getTraceDetail(@PathVariable String traceId) {
        return Result.success(auditService.getTraceDetail(traceId));
    }

    @Operation(summary = "审计统计")
    @GetMapping("/audit/stats")
    public Result<List<Map<String, Object>>> getAuditStats(
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "7d") String timeRange) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(auditService.getAuditStats(module, tenantId, timeRange));
    }

    @Operation(summary = "链路统计")
    @GetMapping("/trace/stats")
    public Result<List<Map<String, Object>>> getTraceStats(
            @RequestParam(defaultValue = "7d") String timeRange) {
        return Result.success(auditService.getTraceStats(timeRange));
    }

    @Operation(summary = "记录审计日志")
    @PostMapping("/audit/record")
    public Result<Void> recordAuditLog(@RequestBody Map<String, Object> auditData) {
        String module = (String) auditData.get("module");
        String businessType = (String) auditData.get("businessType");
        String operation = (String) auditData.get("operation");
        Long businessId = auditData.get("businessId") != null
                ? Long.valueOf(auditData.get("businessId").toString())
                : null;
        String businessNo = (String) auditData.get("businessNo");
        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getUsername();
        Long tenantId = UserContext.getTenantId();
        String ip = (String) auditData.get("ip");
        String requestParams = auditData.get("requestParams") != null
                ? auditData.get("requestParams").toString()
                : null;
        String responseResult = auditData.get("responseResult") != null
                ? auditData.get("responseResult").toString()
                : null;
        Integer status = auditData.get("status") != null
                ? Integer.valueOf(auditData.get("status").toString())
                : 0;
        Long costTime = auditData.get("costTime") != null
                ? Long.valueOf(auditData.get("costTime").toString())
                : 0L;
        String traceId = (String) auditData.get("traceId");

        auditService.recordAuditLog(module, businessType, operation, businessId,
                businessNo, operatorId, operatorName, null, tenantId,
                ip, null, requestParams, responseResult, status, costTime, traceId);

        return Result.success();
    }

    @Operation(summary = "导出审计日志")
    @GetMapping("/audit/export")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Long tenantId = UserContext.getTenantId();
        byte[] data = auditService.exportAuditLogs(module, businessType, tenantId, startTime, endTime);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "audit_logs.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @Operation(summary = "清理过期日志")
    @PostMapping("/clean")
    public Result<Void> cleanExpiredLogs() {
        auditService.cleanExpiredLogs();
        return Result.success();
    }
}
