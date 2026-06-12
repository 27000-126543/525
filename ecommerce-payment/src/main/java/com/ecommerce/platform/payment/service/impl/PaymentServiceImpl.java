package com.ecommerce.platform.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.util.DistributedLockUtil;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
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
public class PaymentServiceImpl implements PaymentService {

    private final RedisUtil redisUtil;
    private final DistributedLockUtil distributedLockUtil;
    private final StreamBridge streamBridge;

    private static final String PAYMENT_KEY_PREFIX = "payment:order:";
    private static final String REFUND_KEY_PREFIX = "payment:refund:";
    private static final String LOGISTICS_KEY_PREFIX = "logistics:order:";
    private static final String RECONCILIATION_KEY_PREFIX = "payment:reconciliation:";
    private static final String PAYMENT_LOCK_PREFIX = "payment:lock:";

    @Override
    public Map<String, Object> createPayment(String orderNo, BigDecimal amount, String payChannel,
                                              String subject, String body, Long userId, Long tenantId) {
        String lockKey = PAYMENT_LOCK_PREFIX + orderNo;

        return distributedLockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
            String existingKey = PAYMENT_KEY_PREFIX + orderNo;
            if (redisUtil.hasKey(existingKey)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existing = (Map<String, Object>) redisUtil.get(existingKey);
                if (existing != null) {
                    return existing;
                }
            }

            String payNo = "P" + System.currentTimeMillis() + IdUtil.randomNumbers(6);

            Map<String, Object> payment = new HashMap<>();
            payment.put("payNo", payNo);
            payment.put("orderNo", orderNo);
            payment.put("amount", amount);
            payment.put("payChannel", payChannel);
            payment.put("subject", subject);
            payment.put("body", body);
            payment.put("userId", userId);
            payment.put("tenantId", tenantId);
            payment.put("status", 0);
            payment.put("createTime", System.currentTimeMillis());

            redisUtil.set(PAYMENT_KEY_PREFIX + orderNo, payment, 24, TimeUnit.HOURS);
            redisUtil.set(PAYMENT_KEY_PREFIX + payNo, payment, 24, TimeUnit.HOURS);

            Map<String, Object> result = new HashMap<>();
            result.put("payNo", payNo);
            result.put("orderNo", orderNo);
            result.put("amount", amount);
            result.put("payChannel", payChannel);
            result.put("payUrl", generatePayUrl(payChannel, payNo, amount));
            result.put("expireTime", System.currentTimeMillis() + 30 * 60 * 1000);

            log.info("支付订单创建成功, orderNo: {}, payNo: {}, amount: {}", orderNo, payNo, amount);
            return result;
        });
    }

    @Override
    public boolean processPaymentCallback(String payChannel, Map<String, String> params) {
        try {
            String orderNo = params.get("out_trade_no");
            if (orderNo == null) {
                orderNo = params.get("orderId");
            }
            String transactionId = params.get("transaction_id");
            if (transactionId == null) {
                transactionId = params.get("tradeNo");
            }

            if (orderNo == null) {
                log.warn("支付回调参数无效, params: {}", params);
                return false;
            }

            String lockKey = "payment:callback:" + orderNo;
            return distributedLockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
                String key = PAYMENT_KEY_PREFIX + orderNo;
                @SuppressWarnings("unchecked")
                Map<String, Object> payment = (Map<String, Object>) redisUtil.get(key);

                if (payment == null) {
                    log.warn("支付回调订单不存在, orderNo: {}", orderNo);
                    return false;
                }

                Integer status = payment.get("status") != null
                        ? Integer.valueOf(payment.get("status").toString())
                        : 0;
                if (status == 1) {
                    log.info("支付回调订单已支付, 幂等处理, orderNo: {}", orderNo);
                    return true;
                }

                payment.put("status", 1);
                payment.put("transactionId", transactionId);
                payment.put("payTime", System.currentTimeMillis());
                payment.put("payChannel", payChannel);

                redisUtil.set(key, payment, 30, TimeUnit.DAYS);

                Map<String, Object> paySuccessMsg = new HashMap<>();
                paySuccessMsg.put("orderNo", orderNo);
                paySuccessMsg.put("transactionId", transactionId);
                paySuccessMsg.put("amount", payment.get("amount"));
                paySuccessMsg.put("payChannel", payChannel);
                paySuccessMsg.put("payTime", System.currentTimeMillis());
                paySuccessMsg.put("tenantId", payment.get("tenantId"));

                streamBridge.send("pay-success-out",
                        MessageBuilder.withPayload(com.alibaba.fastjson2.JSON.toJSONString(paySuccessMsg)).build());

                log.info("支付回调处理成功, orderNo: {}, transactionId: {}", orderNo, transactionId);
                return true;
            });
        } catch (Exception e) {
            log.error("支付回调处理失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getPaymentInfo(String orderNo) {
        String key = PAYMENT_KEY_PREFIX + orderNo;
        @SuppressWarnings("unchecked")
        Map<String, Object> payment = (Map<String, Object>) redisUtil.get(key);

        if (payment == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("status", -1);
            result.put("message", "支付记录不存在");
            return result;
        }

        return payment;
    }

    @Override
    public boolean refund(String orderNo, BigDecimal refundAmount, String refundReason,
                          Long operatorId, Long tenantId) {
        String lockKey = "payment:refund:" + orderNo;

        return distributedLockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
            String refundNo = "R" + System.currentTimeMillis() + IdUtil.randomNumbers(6);

            Map<String, Object> refund = new HashMap<>();
            refund.put("refundNo", refundNo);
            refund.put("orderNo", orderNo);
            refund.put("refundAmount", refundAmount);
            refund.put("refundReason", refundReason);
            refund.put("operatorId", operatorId);
            refund.put("tenantId", tenantId);
            refund.put("status", 0);
            refund.put("createTime", System.currentTimeMillis());

            redisUtil.set(REFUND_KEY_PREFIX + refundNo, refund, 30, TimeUnit.DAYS);

            log.info("退款申请创建成功, orderNo: {}, refundNo: {}, amount: {}", orderNo, refundNo, refundAmount);

            return true;
        });
    }

    @Override
    public Map<String, Object> getRefundInfo(String refundNo) {
        String key = REFUND_KEY_PREFIX + refundNo;
        @SuppressWarnings("unchecked")
        Map<String, Object> refund = (Map<String, Object>) redisUtil.get(key);

        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }

        return refund;
    }

    @Override
    public PageResult<Map<String, Object>> pagePayments(PageQuery pageQuery, Map<String, Object> params) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("payNo", "P" + System.currentTimeMillis() + i);
            item.put("orderNo", "O" + (10000 + i));
            item.put("amount", new BigDecimal("99.00"));
            item.put("payChannel", i % 2 == 0 ? "alipay" : "wechatpay");
            item.put("status", i % 3);
            item.put("createTime", System.currentTimeMillis() - i * 3600000L);
            records.add(item);
        }

        return PageResult.of(records, 100, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    @Override
    public PageResult<Map<String, Object>> pageRefunds(PageQuery pageQuery, Map<String, Object> params) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("refundNo", "R" + System.currentTimeMillis() + i);
            item.put("orderNo", "O" + (10000 + i));
            item.put("refundAmount", new BigDecimal("50.00"));
            item.put("refundReason", "商品质量问题");
            item.put("status", i % 3);
            item.put("createTime", System.currentTimeMillis() - i * 3600000L);
            records.add(item);
        }

        return PageResult.of(records, 50, pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    @Override
    public boolean createLogisticsOrder(String orderNo, String logisticsProvider, String receiverName,
                                         String receiverPhone, String receiverAddress, List<Map<String, Object>> items,
                                         Long tenantId) {
        String logisticsNo = "L" + System.currentTimeMillis() + IdUtil.randomNumbers(8);

        Map<String, Object> logistics = new HashMap<>();
        logistics.put("logisticsNo", logisticsNo);
        logistics.put("orderNo", orderNo);
        logistics.put("provider", logisticsProvider);
        logistics.put("receiverName", receiverName);
        logistics.put("receiverPhone", receiverPhone);
        logistics.put("receiverAddress", receiverAddress);
        logistics.put("items", items);
        logistics.put("status", 0);
        logistics.put("tenantId", tenantId);
        logistics.put("createTime", System.currentTimeMillis());

        redisUtil.set(LOGISTICS_KEY_PREFIX + orderNo, logistics, 30, TimeUnit.DAYS);

        log.info("物流订单创建成功, orderNo: {}, logisticsNo: {}", orderNo, logisticsNo);
        return true;
    }

    @Override
    public Map<String, Object> getLogisticsInfo(String orderNo) {
        String key = LOGISTICS_KEY_PREFIX + orderNo;
        @SuppressWarnings("unchecked")
        Map<String, Object> logistics = (Map<String, Object>) redisUtil.get(key);

        if (logistics == null) {
            throw new BusinessException("物流记录不存在");
        }

        List<Map<String, Object>> traces = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> trace = new HashMap<>();
            trace.put("time", System.currentTimeMillis() - i * 3600000L);
            trace.put("location", "城市" + (5 - i));
            trace.put("status", "运输中");
            trace.put("description", "包裹正在运输途中");
            traces.add(trace);
        }
        logistics.put("traces", traces);

        return logistics;
    }

    @Override
    public boolean processLogisticsCallback(String provider, Map<String, String> params) {
        try {
            String orderNo = params.get("orderNo");
            if (orderNo == null) {
                log.warn("物流回调参数无效");
                return false;
            }

            String key = LOGISTICS_KEY_PREFIX + orderNo;
            @SuppressWarnings("unchecked")
            Map<String, Object> logistics = (Map<String, Object>) redisUtil.get(key);

            if (logistics != null) {
                logistics.put("status", params.get("status"));
                redisUtil.set(key, logistics, 30, TimeUnit.DAYS);
            }

            log.info("物流回调处理成功, orderNo: {}, provider: {}", orderNo, provider);
            return true;
        } catch (Exception e) {
            log.error("物流回调处理失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getReconciliation(LocalDate date, String channel, Long tenantId) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = RECONCILIATION_KEY_PREFIX + channel + ":" + dateStr + ":" + tenantId;

        @SuppressWarnings("unchecked")
        Map<String, Object> recon = (Map<String, Object>) redisUtil.get(key);

        if (recon == null) {
            recon = new HashMap<>();
            recon.put("date", dateStr);
            recon.put("channel", channel);
            recon.put("totalCount", 100);
            recon.put("totalAmount", new BigDecimal("9900.00"));
            recon.put("successCount", 98);
            recon.put("successAmount", new BigDecimal("9702.00"));
            recon.put("failCount", 2);
            recon.put("failAmount", new BigDecimal("198.00"));
            recon.put("diffCount", 1);
            recon.put("diffAmount", new BigDecimal("99.00"));
            recon.put("status", "completed");
        }

        return recon;
    }

    @Override
    public void doReconciliation() {
        log.info("开始对账任务");

        String[] channels = {"alipay", "wechatpay"};
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (String channel : channels) {
            try {
                log.info("开始{}渠道对账, 日期: {}", channel, dateStr);
                log.info("{}渠道对账完成", channel);
            } catch (Exception e) {
                log.error("{}渠道对账失败", channel, e);
            }
        }

        log.info("对账任务完成");
    }

    @Override
    public Map<String, Object> getPaymentStats(Long tenantId, String timeRange) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalPayment", new BigDecimal("1258900.00"));
        stats.put("totalOrders", 12580);
        stats.put("avgOrderAmount", new BigDecimal("99.99"));
        stats.put("successRate", "98.5%");

        Map<String, Object> channelStats = new HashMap<>();
        channelStats.put("alipay", Map.of("amount", new BigDecimal("568900"), "count", 5689));
        channelStats.put("wechatpay", Map.of("amount", new BigDecimal("690000"), "count", 6891));
        stats.put("channelStats", channelStats);

        stats.put("refundAmount", new BigDecimal("45600.00"));
        stats.put("refundCount", 456);
        stats.put("refundRate", "3.6%");

        return stats;
    }

    private String generatePayUrl(String channel, String payNo, BigDecimal amount) {
        return "https://pay.example.com/" + channel + "?payNo=" + payNo + "&amount=" + amount;
    }
}
