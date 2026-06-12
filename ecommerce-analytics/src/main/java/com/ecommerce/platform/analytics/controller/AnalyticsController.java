package com.ecommerce.platform.analytics.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "数据分析")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "实时概览数据")
    @GetMapping("/realtime/overview")
    public Result<Map<String, Object>> getRealtimeOverview() {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getRealtimeOverview(tenantId));
    }

    @Operation(summary = "仪表板数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(
            @RequestParam(defaultValue = "7d") String timeRange) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getDashboardData(tenantId, timeRange));
    }

    @Operation(summary = "销售趋势")
    @GetMapping("/sales/trend")
    public Result<Map<String, Object>> getSalesTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "hour") String dimension) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getSalesTrend(tenantId, startTime, endTime, dimension));
    }

    @Operation(summary = "转化漏斗")
    @GetMapping("/conversion/funnel")
    public Result<Map<String, Object>> getConversionFunnel(
            @RequestParam(defaultValue = "7d") String timeRange) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getConversionFunnel(tenantId, timeRange));
    }

    @Operation(summary = "库存分析")
    @GetMapping("/inventory")
    public Result<Map<String, Object>> getInventoryAnalysis() {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getInventoryAnalysis(tenantId));
    }

    @Operation(summary = "渠道分析")
    @GetMapping("/channel")
    public Result<Map<String, Object>> getChannelAnalysis(
            @RequestParam(defaultValue = "7d") String timeRange) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getChannelAnalysis(tenantId, timeRange));
    }

    @Operation(summary = "用户分析")
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserAnalysis(
            @RequestParam(defaultValue = "7d") String timeRange) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getUserAnalysis(tenantId, timeRange));
    }

    @Operation(summary = "商品排行榜")
    @GetMapping("/product/ranking")
    public Result<Map<String, Object>> getProductRanking(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getProductRanking(tenantId, category, limit));
    }

    @Operation(summary = "自定义报表")
    @PostMapping("/report")
    public Result<List<Map<String, Object>>> getCustomReport(
            @RequestParam String reportType,
            @RequestBody Map<String, Object> params) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getCustomReport(tenantId, reportType, params));
    }

    @Operation(summary = "预警列表")
    @GetMapping("/alerts")
    public Result<List<Map<String, Object>>> getAlerts(
            @RequestParam(required = false) Integer status) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(analyticsService.getAlerts(tenantId, status));
    }

    @Operation(summary = "添加预警规则")
    @PostMapping("/alert/rule")
    public Result<Void> addAlertRule(@RequestBody Map<String, Object> rule) {
        analyticsService.addAlertRule(rule);
        return Result.success();
    }
}
