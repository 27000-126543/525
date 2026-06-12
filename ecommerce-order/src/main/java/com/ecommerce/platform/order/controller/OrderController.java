package com.ecommerce.platform.order.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.order.dto.OrderCreateDTO;
import com.ecommerce.platform.order.entity.OrderInfo;
import com.ecommerce.platform.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "获取订单Token")
    @GetMapping("/token")
    public Result<String> getOrderToken() {
        Long userId = UserContext.getUserId();
        return Result.success(orderService.generateOrderToken(userId));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderInfo> createOrder(@RequestBody OrderCreateDTO dto) {
        dto.setUserId(UserContext.getUserId());
        return Result.success(orderService.createOrder(dto));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderInfo> getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @Operation(summary = "根据订单号获取订单")
    @GetMapping("/orderNo/{orderNo}")
    public Result<OrderInfo> getOrderByOrderNo(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderByOrderNo(orderNo));
    }

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<PageResult<OrderInfo>> page(PageQuery pageQuery, OrderInfo order) {
        return Result.success(orderService.page(pageQuery, order));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/my")
    public Result<List<OrderInfo>> myOrders(@RequestParam(required = false) Integer status) {
        Long userId = UserContext.getUserId();
        return Result.success(orderService.listByUserId(userId, status));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestParam String reason) {
        orderService.cancelOrder(id, reason);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/confirmReceive")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        orderService.confirmReceive(id);
        return Result.success();
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success();
    }
}
