package com.ecommerce.platform.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionDTO implements Serializable {

    private String orderNo;
    private Long userId;
    private Long tenantId;
    private String source;
    private List<StockItem> items;
    private String deductionId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItem implements Serializable {
        private Long skuId;
        private Long spuId;
        private Integer quantity;
    }
}
