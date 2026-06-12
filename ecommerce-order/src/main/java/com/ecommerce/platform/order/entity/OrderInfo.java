package com.ecommerce.platform.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.platform.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
public class OrderInfo extends BaseEntity {

    private Long id;
    private String orderNo;
    private Long userId;
    private Integer orderType;
    private Integer orderSource;
    private String channelCode;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
    private BigDecimal couponAmount;
    private Integer status;
    private Integer payStatus;
    private Integer payType;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String remark;
    private Long couponId;
    private Long activityId;
    private String outTradeNo;
    private String transactionId;
    private Integer invoiceType;
    private String invoiceTitle;
    private String invoiceTaxNo;
    private Integer pointsUsed;
    private BigDecimal pointsDeducted;
    private Integer deleteFlag;
}
