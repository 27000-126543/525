package com.ecommerce.platform.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    TENANT_NOT_FOUND(1001, "租户不存在"),
    TENANT_DISABLED(1002, "租户已禁用"),

    USER_NOT_FOUND(1101, "用户不存在"),
    USER_PASSWORD_ERROR(1102, "密码错误"),
    USER_DISABLED(1103, "用户已禁用"),
    USER_NOT_LOGIN(1104, "用户未登录"),
    USER_TOKEN_EXPIRED(1105, "Token已过期"),
    USER_PERMISSION_DENIED(1106, "权限不足"),

    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "库存不足"),
    PRODUCT_OFF_SHELF(2003, "商品已下架"),

    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_REPEAT_SUBMIT(3003, "重复提交订单"),
    ORDER_CREATE_FAILED(3004, "订单创建失败"),

    PAYMENT_FAILED(4001, "支付失败"),
    PAYMENT_CALLBACK_ERROR(4002, "支付回调异常"),
    PAYMENT_AMOUNT_MISMATCH(4003, "支付金额不匹配"),

    SECKILL_NOT_START(5001, "秒杀未开始"),
    SECKILL_ENDED(5002, "秒杀已结束"),
    SECKILL_SOLD_OUT(5003, "商品已售罄"),
    SECKILL_LIMIT_EXCEED(5004, "超出限购数量"),

    APPROVAL_NOT_FOUND(6001, "审批记录不存在"),
    APPROVAL_STATUS_ERROR(6002, "审批状态错误"),
    APPROVAL_NODE_NOT_FOUND(6003, "审批节点不存在"),

    MESSAGE_SEND_FAILED(7001, "消息发送失败"),
    MESSAGE_CHANNEL_NOT_SUPPORTED(7002, "不支持的消息渠道"),

    RATE_LIMIT_EXCEEDED(8001, "超出限流阈值"),

    AUDIT_LOG_FAILED(9001, "审计日志记录失败");

    private final Integer code;
    private final String message;
}
