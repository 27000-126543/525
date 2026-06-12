package com.ecommerce.platform.payment.service;

import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    Map<String, Object> createPayment(String orderNo, BigDecimal amount, String payChannel,
                                       String subject, String body, Long userId, Long tenantId);

    boolean processPaymentCallback(String payChannel, Map<String, String> params);

    Map<String, Object> getPaymentInfo(String orderNo);

    boolean refund(String orderNo, BigDecimal refundAmount, String refundReason,
                   Long operatorId, Long tenantId);

    Map<String, Object> getRefundInfo(String refundNo);

    PageResult<Map<String, Object>> pagePayments(PageQuery pageQuery, Map<String, Object> params);

    PageResult<Map<String, Object>> pageRefunds(PageQuery pageQuery, Map<String, Object> params);

    boolean createLogisticsOrder(String orderNo, String logisticsProvider, String receiverName,
                                  String receiverPhone, String receiverAddress, List<Map<String, Object>> items,
                                  Long tenantId);

    Map<String, Object> getLogisticsInfo(String orderNo);

    boolean processLogisticsCallback(String provider, Map<String, String> params);

    Map<String, Object> getReconciliation(LocalDate date, String channel, Long tenantId);

    void doReconciliation();

    Map<String, Object> getPaymentStats(Long tenantId, String timeRange);
}
