package com.ecommerce.platform.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO implements Serializable {

    private String orderToken;
    private Long userId;
    private Integer orderType;
    private Integer orderSource;
    private String channelCode;
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private Long couponId;
    private Long activityId;
    private Integer payType;
    private String remark;
    private Integer invoiceType;
    private String invoiceTitle;
    private String invoiceTaxNo;
    private Integer pointsUsed;

    private List<OrderItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO implements Serializable {
        private Long skuId;
        private Long spuId;
        private Integer quantity;
        private Long cartId;
    }
}
