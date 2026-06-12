package com.ecommerce.platform.analytics.service.impl;

import cn.hutool.core.date.DateUtil;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final RedisUtil redisUtil;

    private static final String REALTIME_KEY = "analytics:realtime:";
    private static final String SALES_KEY = "analytics:sales:";
    private static final String ORDER_KEY = "analytics:orders:";
    private static final String CHANNEL_KEY = "analytics:channel:";
    private static final String USER_KEY = "analytics:users:";
    private static final String PRODUCT_KEY = "analytics:product:";
    private static final String ALERT_KEY = "analytics:alerts:";

    @Override
    public Map<String, Object> getRealtimeOverview(Long tenantId) {
        Map<String, Object> result = new HashMap<>();
        String today = DateUtil.today();
        String keyPrefix = REALTIME_KEY + tenantId + ":";

        result.put("todayOrders", getRedisCounter(keyPrefix + "orders:today:" + today));
        result.put("todaySales", getRedisAmount(keyPrefix + "sales:today:" + today));
        result.put("todayUsers", getRedisCounter(keyPrefix + "users:today:" + today));
        result.put("todayViews", getRedisCounter(keyPrefix + "views:today:" + today));
        result.put("conversionRate", calculateConversionRate(tenantId));
        result.put("avgOrderAmount", calculateAvgOrderAmount(tenantId));

        return result;
    }

    @Override
    public Map<String, Object> getDashboardData(Long tenantId, String timeRange) {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime startTime = calculateStartTime(timeRange);
        LocalDateTime endTime = LocalDateTime.now();

        result.put("timeRange", timeRange);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("realtime", getRealtimeOverview(tenantId));
        result.put("salesTrend", getSalesTrend(tenantId, startTime, endTime, "hour"));
        result.put("conversionFunnel", getConversionFunnel(tenantId, timeRange));
        result.put("channelAnalysis", getChannelAnalysis(tenantId, timeRange));
        result.put("topProducts", getProductRanking(tenantId, null, 10));

        return result;
    }

    @Override
    public Map<String, Object> getSalesTrend(Long tenantId, LocalDateTime startTime, LocalDateTime endTime, String dimension) {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> salesData = new ArrayList<>();
        List<Integer> orderData = new ArrayList<>();

        LocalDateTime current = startTime;
        String keyPrefix = SALES_KEY + tenantId + ":";

        while (current.isBefore(endTime)) {
            String timeKey = formatTimeKey(current, dimension);
            labels.add(timeKey);

            Object sales = redisUtil.hGet(keyPrefix + dimension, timeKey);
            Object orders = redisUtil.hGet(ORDER_KEY + tenantId + ":" + dimension, timeKey);

            salesData.add(sales != null ? new BigDecimal(sales.toString()) : BigDecimal.ZERO);
            orderData.add(orders != null ? Integer.parseInt(orders.toString()) : 0);

            current = incrementTime(current, dimension);
        }

        result.put("labels", labels);
        result.put("sales", salesData);
        result.put("orders", orderData);

        return result;
    }

    @Override
    public Map<String, Object> getConversionFunnel(Long tenantId, String timeRange) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> funnel = new ArrayList<>();

        String keyPrefix = USER_KEY + tenantId + ":";

        funnel.add(createFunnelItem("访客数", getRedisCounter(keyPrefix + "visitors:" + timeRange), 100));
        funnel.add(createFunnelItem("浏览商品", getRedisCounter(keyPrefix + "productViews:" + timeRange), 85));
        funnel.add(createFunnelItem("加入购物车", getRedisCounter(keyPrefix + "cartAdds:" + timeRange), 45));
        funnel.add(createFunnelItem("提交订单", getRedisCounter(keyPrefix + "orderCreates:" + timeRange), 25));
        funnel.add(createFunnelItem("支付成功", getRedisCounter(keyPrefix + "paySuccess:" + timeRange), 20));

        result.put("funnel", funnel);
        result.put("overallConversionRate", "20%");
        result.put("cartConversionRate", "55.6%");
        result.put("payConversionRate", "80%");

        return result;
    }

    @Override
    public Map<String, Object> getInventoryAnalysis(Long tenantId) {
        Map<String, Object> result = new HashMap<>();

        result.put("totalSkuCount", 12580);
        result.put("availableSkuCount", 11230);
        result.put("outOfStockCount", 1350);
        result.put("lowStockCount", 890);
        result.put("stockTurnoverDays", 45);
        result.put("stockValue", new BigDecimal("8923500.00"));

        List<Map<String, Object>> categoryStock = new ArrayList<>();
        categoryStock.add(createCategoryStock("手机数码", 2340, new BigDecimal("3200000")));
        categoryStock.add(createCategoryStock("服装鞋帽", 3560, new BigDecimal("1800000")));
        categoryStock.add(createCategoryStock("家居日用", 2890, new BigDecimal("980000")));
        categoryStock.add(createCategoryStock("食品饮料", 1890, new BigDecimal("750000")));
        result.put("categoryStock", categoryStock);

        return result;
    }

    @Override
    public Map<String, Object> getChannelAnalysis(Long tenantId, String timeRange) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> channels = new ArrayList<>();

        String keyPrefix = CHANNEL_KEY + tenantId + ":";

        channels.add(createChannelData("自有商城", "app", 2345, new BigDecimal("568900"), 28.5));
        channels.add(createChannelData("微信小程序", "wechat", 1890, new BigDecimal("345600"), 21.3));
        channels.add(createChannelData("天猫旗舰店", "tmall", 3200, new BigDecimal("789000"), 32.1));
        channels.add(createChannelData("京东自营", "jd", 1560, new BigDecimal("456700"), 12.8));
        channels.add(createChannelData("线下门店", "offline", 980, new BigDecimal("234500"), 5.3));

        result.put("channels", channels);
        result.put("totalChannels", 5);
        result.put("topChannel", "天猫旗舰店");

        return result;
    }

    @Override
    public Map<String, Object> getUserAnalysis(Long tenantId, String timeRange) {
        Map<String, Object> result = new HashMap<>();

        result.put("totalUsers", 125680);
        result.put("newUsers", 3450);
        result.put("activeUsers", 45680);
        result.put("payingUsers", 28900);
        result.put("userGrowthRate", 12.5);
        result.put("retentionRate_7d", 45.6);
        result.put("retentionRate_30d", 28.3);

        List<Map<String, Object>> ageDistribution = new ArrayList<>();
        ageDistribution.add(createUserDistribution("18-24岁", 15.2));
        ageDistribution.add(createUserDistribution("25-34岁", 38.5));
        ageDistribution.add(createUserDistribution("35-44岁", 25.8));
        ageDistribution.add(createUserDistribution("45-54岁", 14.3));
        ageDistribution.add(createUserDistribution("55岁以上", 6.2));
        result.put("ageDistribution", ageDistribution);

        return result;
    }

    @Override
    public Map<String, Object> getProductRanking(Long tenantId, String category, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> ranking = new ArrayList<>();
        String key = PRODUCT_KEY + tenantId + ":ranking:sales";

        if (limit == null || limit < 1) {
            limit = 10;
        }

        Set<Object> topProducts = redisUtil.zReverseRange(key, 0, limit - 1);
        int rank = 1;
        if (topProducts != null) {
            for (Object productId : topProducts) {
                Map<String, Object> item = new HashMap<>();
                item.put("rank", rank++);
                item.put("productId", productId);
                item.put("productName", "商品" + productId);
                item.put("sales", 1000 - rank * 50);
                item.put("amount", new BigDecimal(50000 - rank * 2000));
                ranking.add(item);
            }
        }

        result.put("ranking", ranking);
        result.put("total", limit);

        return result;
    }

    @Override
    public List<Map<String, Object>> getCustomReport(Long tenantId, String reportType, Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i + 1);
            row.put("tenantId", tenantId);
            row.put("reportType", reportType);
            row.put("date", LocalDate.now().minusDays(i));
            row.put("value", 1000 - i * 50);
            result.add(row);
        }

        return result;
    }

    @Override
    public void recordOrderEvent(Map<String, Object> orderData) {
        try {
            Long tenantId = Long.valueOf(orderData.get("tenantId").toString());
            BigDecimal amount = new BigDecimal(orderData.get("amount").toString());
            String channel = orderData.get("channel") != null ? orderData.get("channel").toString() : "default";

            String today = DateUtil.today();
            String hourKey = DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:00:00");

            String realtimePrefix = REALTIME_KEY + tenantId + ":";
            redisUtil.increment(realtimePrefix + "orders:today:" + today);
            redisUtil.hIncr(realtimePrefix + "sales:today:" + today, "total", amount.doubleValue());

            String salesPrefix = SALES_KEY + tenantId + ":hour:";
            redisUtil.hIncr(salesPrefix, hourKey, amount.doubleValue());

            String orderPrefix = ORDER_KEY + tenantId + ":hour:";
            redisUtil.hIncr(orderPrefix, hourKey, 1);

            String channelKey = CHANNEL_KEY + tenantId + ":" + channel;
            redisUtil.increment(channelKey + ":orders");
            redisUtil.hIncr(channelKey + ":sales", today, amount.doubleValue());

            log.debug("订单事件已记录, tenantId: {}, amount: {}", tenantId, amount);
        } catch (Exception e) {
            log.error("记录订单事件失败", e);
        }
    }

    @Override
    public void recordProductEvent(Map<String, Object> productData) {
        try {
            Long tenantId = Long.valueOf(productData.get("tenantId").toString());
            Long productId = Long.valueOf(productData.get("productId").toString());
            String eventType = productData.get("eventType") != null ? productData.get("eventType").toString() : "view";

            String key = PRODUCT_KEY + tenantId + ":ranking:sales";
            double score = switch (eventType) {
                case "view" -> 1;
                case "cart" -> 5;
                case "buy" -> 50;
                default -> 1;
            };

            redisUtil.zIncrementScore(key, String.valueOf(productId), score);

            log.debug("商品事件已记录, tenantId: {}, productId: {}, eventType: {}", tenantId, productId, eventType);
        } catch (Exception e) {
            log.error("记录商品事件失败", e);
        }
    }

    @Override
    public void recordUserBehavior(Map<String, Object> behaviorData) {
        try {
            Long tenantId = Long.valueOf(behaviorData.get("tenantId").toString());
            Long userId = behaviorData.get("userId") != null ? Long.valueOf(behaviorData.get("userId").toString()) : null;
            String behaviorType = behaviorData.get("behaviorType") != null ? behaviorData.get("behaviorType").toString() : "view";

            String today = DateUtil.today();
            String keyPrefix = USER_KEY + tenantId + ":";

            switch (behaviorType) {
                case "login" -> {
                    redisUtil.increment(keyPrefix + "users:today:" + today);
                    redisUtil.sAdd(keyPrefix + "active:today:" + today, String.valueOf(userId));
                }
                case "view" -> redisUtil.increment(keyPrefix + "productViews:today:" + today);
                case "cart" -> redisUtil.increment(keyPrefix + "cartAdds:today:" + today);
                case "order" -> redisUtil.increment(keyPrefix + "orderCreates:today:" + today);
                case "pay" -> redisUtil.increment(keyPrefix + "paySuccess:today:" + today);
            }

            log.debug("用户行为已记录, tenantId: {}, userId: {}, behaviorType: {}", tenantId, userId, behaviorType);
        } catch (Exception e) {
            log.error("记录用户行为失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getAlerts(Long tenantId, Integer status) {
        List<Map<String, Object>> alerts = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", i + 1);
            alert.put("type", i % 3 == 0 ? "stock" : i % 3 == 1 ? "sales" : "system");
            alert.put("level", i % 2 == 0 ? "high" : "medium");
            alert.put("title", "异常预警" + (i + 1));
            alert.put("message", "检测到数据异常，请及时处理");
            alert.put("status", status != null ? status : 0);
            alert.put("createTime", LocalDateTime.now().minusHours(i));
            alerts.add(alert);
        }

        return alerts;
    }

    @Override
    public void addAlertRule(Map<String, Object> rule) {
        log.info("添加预警规则: {}", rule);
    }

    @Override
    public void checkAlerts() {
        log.debug("开始检查预警");
    }

    private Integer getRedisCounter(String key) {
        Object value = redisUtil.get(key);
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }

    private BigDecimal getRedisAmount(String key) {
        Object value = redisUtil.get(key);
        return value != null ? new BigDecimal(value.toString()) : BigDecimal.ZERO;
    }

    private String formatTimeKey(LocalDateTime time, String dimension) {
        DateTimeFormatter formatter = switch (dimension) {
            case "hour" -> DateTimeFormatter.ofPattern("MM-dd HH:00");
            case "day" -> DateTimeFormatter.ofPattern("MM-dd");
            case "month" -> DateTimeFormatter.ofPattern("yyyy-MM");
            default -> DateTimeFormatter.ofPattern("MM-dd HH:mm");
        };
        return time.format(formatter);
    }

    private LocalDateTime incrementTime(LocalDateTime time, String dimension) {
        return switch (dimension) {
            case "hour" -> time.plusHours(1);
            case "day" -> time.plusDays(1);
            case "month" -> time.plusMonths(1);
            default -> time.plusMinutes(5);
        };
    }

    private LocalDateTime calculateStartTime(String timeRange) {
        return switch (timeRange) {
            case "today" -> LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            case "yesterday" -> LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "30d" -> LocalDateTime.now().minusDays(30);
            case "90d" -> LocalDateTime.now().minusDays(90);
            default -> LocalDateTime.now().minusDays(7);
        };
    }

    private Map<String, Object> createFunnelItem(String name, int value, double rate) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        item.put("conversionRate", rate + "%");
        return item;
    }

    private Map<String, Object> createCategoryStock(String category, int count, BigDecimal value) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", category);
        item.put("count", count);
        item.put("value", value);
        return item;
    }

    private Map<String, Object> createChannelData(String name, String code, int orders, BigDecimal amount, double ratio) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("code", code);
        item.put("orders", orders);
        item.put("amount", amount);
        item.put("ratio", ratio);
        return item;
    }

    private Map<String, Object> createUserDistribution(String ageRange, double ratio) {
        Map<String, Object> item = new HashMap<>();
        item.put("ageRange", ageRange);
        item.put("ratio", ratio);
        return item;
    }

    private BigDecimal calculateConversionRate(Long tenantId) {
        return new BigDecimal("3.45");
    }

    private BigDecimal calculateAvgOrderAmount(Long tenantId) {
        return new BigDecimal("128.50");
    }
}
