package com.ecommerce.platform.payment.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "支付管理")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "创建支付")
    @PostMapping("/create")
    public Result<Map<String, Object>> createPayment(
            @RequestParam String orderNo,
            @RequestParam BigDecimal amount,
            @RequestParam String payChannel,
            @RequestParam String subject,
            @RequestParam(required = false) String body) {
        Long userId = UserContext.getUserId();
        Long tenantId = UserContext.getTenantId();
        return Result.success(paymentService.createPayment(
                orderNo, amount, payChannel, subject, body, userId, tenantId));
    }

    @Operation(summary = "支付宝回调")
    @PostMapping("/callback/alipay")
    public String alipayCallback(@RequestParam Map<String, String> params) {
        boolean success = paymentService.processPaymentCallback("alipay", params);
        return success ? "success" : "fail";
    }

    @Operation(summary = "微信支付回调")
    @PostMapping("/callback/wechatpay")
    public String wechatpayCallback(@RequestBody Map<String, String> params) {
        boolean success = paymentService.processPaymentCallback("wechatpay", params);
        return success ? "success" : "fail";
    }

    @Operation(summary = "获取支付信息")
    @GetMapping("/info/{orderNo}")
    public Result<Map<String, Object>> getPaymentInfo(@PathVariable String orderNo) {
        return Result.success(paymentService.getPaymentInfo(orderNo));
    }

    @Operation(summary = "申请退款")
    @PostMapping("/refund")
    public Result<Boolean> refund(
            @RequestParam String orderNo,
            @RequestParam BigDecimal refundAmount,
            @RequestParam(required = false) String refundReason) {
        Long userId = UserContext.getUserId();
        Long tenantId = UserContext.getTenantId();
        return Result.success(paymentService.refund(orderNo, refundAmount, refundReason, userId, tenantId));
    }

    @Operation(summary = "获取退款信息")
    @GetMapping("/refund/{refundNo}")
    public Result<Map<String, Object>> getRefundInfo(@PathVariable String refundNo) {
        return Result.success(paymentService.getRefundInfo(refundNo));
    }

    @Operation(summary = "分页查询支付记录")
    @GetMapping("/payment/page")
    public Result<PageResult<Map<String, Object>>> pagePayments(
            PageQuery pageQuery,
            @RequestParam Map<String, Object> params) {
        return Result.success(paymentService.pagePayments(pageQuery, params));
    }

    @Operation(summary = "分页查询退款记录")
    @GetMapping("/refund/page")
    public Result<PageResult<Map<String, Object>>> pageRefunds(
            PageQuery pageQuery,
            @RequestParam Map<String, Object> params) {
        return Result.success(paymentService.pageRefunds(pageQuery, params));
    }

    @Operation(summary = "创建物流订单")
    @PostMapping("/logistics/create")
    public Result<Boolean> createLogisticsOrder(
            @RequestParam String orderNo,
            @RequestParam String logisticsProvider,
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String receiverAddress,
            @RequestParam List<Map<String, Object>> items) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(paymentService.createLogisticsOrder(
                orderNo, logisticsProvider, receiverName, receiverPhone, receiverAddress, items, tenantId));
    }

    @Operation(summary = "获取物流信息")
    @GetMapping("/logistics/{orderNo}")
    public Result<Map<String, Object>> getLogisticsInfo(@PathVariable String orderNo) {
        return Result.success(paymentService.getLogisticsInfo(orderNo));
    }

    @Operation(summary = "物流回调")
    @PostMapping("/logistics/callback/{provider}")
    public String logisticsCallback(@PathVariable String provider, @RequestParam Map<String, String> params) {
        boolean success = paymentService.processLogisticsCallback(provider, params);
        return success ? "success" : "fail";
    }

    @Operation(summary = "获取对账数据")
    @GetMapping("/reconciliation")
    public Result<Map<String, Object>> getReconciliation(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam String channel) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(paymentService.getReconciliation(date, channel, tenantId));
    }

    @Operation(summary = "执行对账")
    @PostMapping("/reconciliation/do")
    public Result<Void> doReconciliation() {
        paymentService.doReconciliation();
        return Result.success();
    }

    @Operation(summary = "支付统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getPaymentStats(
            @RequestParam(defaultValue = "7d") String timeRange) {
        Long tenantId = UserContext.getTenantId();
        return Result.success(paymentService.getPaymentStats(tenantId, timeRange));
    }
}
