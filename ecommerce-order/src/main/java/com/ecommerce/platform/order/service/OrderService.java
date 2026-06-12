package com.ecommerce.platform.order.service;

import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.order.dto.OrderCreateDTO;
import com.ecommerce.platform.order.entity.OrderInfo;

import java.util.List;

public interface OrderService {

    String generateOrderToken(Long userId);

    OrderInfo createOrder(OrderCreateDTO dto);

    OrderInfo getOrderById(Long id);

    OrderInfo getOrderByOrderNo(String orderNo);

    PageResult<OrderInfo> page(PageQuery pageQuery, OrderInfo order);

    List<OrderInfo> listByUserId(Long userId, Integer status);

    void cancelOrder(Long id, String reason);

    void confirmReceive(Long id);

    void deleteOrder(Long id);

    boolean paySuccess(String orderNo, String transactionId);

    void autoCancelOrder();
}
