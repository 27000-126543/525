package com.ecommerce.platform.analytics.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    Map<String, Object> getRealtimeOverview(Long tenantId);

    Map<String, Object> getDashboardData(Long tenantId, String timeRange);

    Map<String, Object> getSalesTrend(Long tenantId, LocalDateTime startTime, LocalDateTime endTime, String dimension);

    Map<String, Object> getConversionFunnel(Long tenantId, String timeRange);

    Map<String, Object> getInventoryAnalysis(Long tenantId);

    Map<String, Object> getChannelAnalysis(Long tenantId, String timeRange);

    Map<String, Object> getUserAnalysis(Long tenantId, String timeRange);

    Map<String, Object> getProductRanking(Long tenantId, String category, Integer limit);

    List<Map<String, Object>> getCustomReport(Long tenantId, String reportType, Map<String, Object> params);

    void recordOrderEvent(Map<String, Object> orderData);

    void recordProductEvent(Map<String, Object> productData);

    void recordUserBehavior(Map<String, Object> behaviorData);

    List<Map<String, Object>> getAlerts(Long tenantId, Integer status);

    void addAlertRule(Map<String, Object> rule);

    void checkAlerts();
}
