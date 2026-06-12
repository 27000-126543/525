package com.ecommerce.platform.order.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.platform.common.constant.RedisConstants;
import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.result.ResultCode;
import com.ecommerce.platform.common.util.DistributedLockUtil;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.order.dto.OrderCreateDTO;
import com.ecommerce.platform.order.entity.OrderInfo;
import com.ecommerce.platform.order.entity.OrderItem;
import com.ecommerce.platform.order.mapper.OrderInfoMapper;
import com.ecommerce.platform.order.mapper.OrderItemMapper;
import com.ecommerce.platform.order.mq.OrderMessageProducer;
import com.ecommerce.platform.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final RedisUtil redisUtil;
    private final DistributedLockUtil distributedLockUtil;
    private final OrderMessageProducer orderMessageProducer;

    @Value("${order.token-prefix:order:token:}")
    private String orderTokenPrefix;

    @Value("${order.token-expire-minutes:30}")
    private Integer tokenExpireMinutes;

    @Override
    public String generateOrderToken(Long userId) {
        String token = IdUtil.fastSimpleUUID();
        String key = orderTokenPrefix + userId + ":" + token;
        redisUtil.set(key, 1, tokenExpireMinutes, TimeUnit.MINUTES);
        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createOrder(OrderCreateDTO dto) {
        Long userId = dto.getUserId();
        if (userId == null) {
            userId = UserContext.getUserId();
        }

        String tokenKey = orderTokenPrefix + userId + ":" + dto.getOrderToken();
        Boolean deleted = redisUtil.delete(tokenKey);
        if (!Boolean.TRUE.equals(deleted)) {
            throw new BusinessException(ResultCode.ORDER_REPEAT_SUBMIT);
        }

        String orderNo = generateOrderNo();
        String orderLockKey = "order:create:" + orderNo;

        return distributedLockUtil.tryLock(orderLockKey, 3, 10, TimeUnit.SECONDS, () -> {
            Long count = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getOrderNo, orderNo));
            if (count > 0) {
                throw new BusinessException(ResultCode.ORDER_REPEAT_SUBMIT);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal payAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
                OrderItem item = new OrderItem();
                item.setSkuId(itemDTO.getSkuId());
                item.setSpuId(itemDTO.getSpuId());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(new BigDecimal("99.00"));
                item.setOriginalPrice(new BigDecimal("199.00"));
                item.setTotalAmount(item.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
                item.setPayAmount(item.getTotalAmount());
                totalAmount = totalAmount.add(item.getTotalAmount());
                payAmount = payAmount.add(item.getPayAmount());
                orderItems.add(item);
            }

            OrderInfo order = new OrderInfo();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : 0);
            order.setOrderSource(dto.getOrderSource() != null ? dto.getOrderSource() : 1);
            order.setChannelCode(dto.getChannelCode());
            order.setTotalAmount(totalAmount);
            order.setPayAmount(payAmount);
            order.setFreightAmount(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setCouponAmount(BigDecimal.ZERO);
            order.setStatus(0);
            order.setPayStatus(0);
            order.setPayType(dto.getPayType());
            order.setReceiverName(dto.getReceiverName());
            order.setReceiverPhone(dto.getReceiverPhone());
            order.setReceiverProvince(dto.getReceiverProvince());
            order.setReceiverCity(dto.getReceiverCity());
            order.setReceiverDistrict(dto.getReceiverDistrict());
            order.setReceiverAddress(dto.getReceiverAddress());
            order.setRemark(dto.getRemark());
            order.setActivityId(dto.getActivityId());

            orderInfoMapper.insert(order);

            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                item.setOrderNo(orderNo);
                orderItemMapper.insert(item);
            }

            orderMessageProducer.sendOrderCreateMessage(orderNo, userId);

            log.info("订单创建成功, orderNo: {}, userId: {}", orderNo, userId);
            return order;
        });
    }

    @Override
    public OrderInfo getOrderById(Long id) {
        return orderInfoMapper.selectById(id);
    }

    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        return orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo));
    }

    @Override
    public PageResult<OrderInfo> page(PageQuery pageQuery, OrderInfo order) {
        Page<OrderInfo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getDeleted, 0);

        if (order.getUserId() != null) {
            wrapper.eq(OrderInfo::getUserId, order.getUserId());
        }
        if (order.getStatus() != null) {
            wrapper.eq(OrderInfo::getStatus, order.getStatus());
        }
        if (order.getOrderNo() != null) {
            wrapper.like(OrderInfo::getOrderNo, order.getOrderNo());
        }
        if (order.getOrderSource() != null) {
            wrapper.eq(OrderInfo::getOrderSource, order.getOrderSource());
        }

        wrapper.orderByDesc(OrderInfo::getCreateTime);
        Page<OrderInfo> result = orderInfoMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public List<OrderInfo> listByUserId(Long userId, Integer status) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getDeleted, 0);
        if (status != null) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return orderInfoMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id, String reason) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        OrderInfo update = new OrderInfo();
        update.setId(id);
        update.setStatus(5);
        update.setCancelTime(LocalDateTime.now());
        update.setCancelReason(reason);
        orderInfoMapper.updateById(update);

        orderMessageProducer.sendOrderCancelMessage(order.getOrderNo(), order.getUserId());

        log.info("订单取消成功, orderId: {}, reason: {}", id, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        if (order.getStatus() != 3) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        OrderInfo update = new OrderInfo();
        update.setId(id);
        update.setStatus(4);
        update.setReceiveTime(LocalDateTime.now());
        orderInfoMapper.updateById(update);

        log.info("确认收货成功, orderId: {}", id);
    }

    @Override
    public void deleteOrder(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        orderInfoMapper.deleteById(id);
        log.info("订单删除成功, orderId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean paySuccess(String orderNo, String transactionId) {
        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            log.warn("支付回调订单不存在, orderNo: {}", orderNo);
            return false;
        }

        if (order.getPayStatus() == 1) {
            log.info("订单已支付, 幂等处理, orderNo: {}", orderNo);
            return true;
        }

        String lockKey = "order:pay:" + orderNo;
        return distributedLockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
            OrderInfo recheck = orderInfoMapper.selectById(order.getId());
            if (recheck.getPayStatus() == 1) {
                return true;
            }

            OrderInfo update = new OrderInfo();
            update.setId(order.getId());
            update.setStatus(1);
            update.setPayStatus(1);
            update.setPayTime(LocalDateTime.now());
            update.setTransactionId(transactionId);
            int rows = orderInfoMapper.updateById(update);

            if (rows > 0) {
                log.info("订单支付成功, orderNo: {}", orderNo);
            }
            return rows > 0;
        });
    }

    @Override
    public void autoCancelOrder() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
        List<OrderInfo> expireOrders = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, 0)
                .eq(OrderInfo::getPayStatus, 0)
                .lt(OrderInfo::getCreateTime, expireTime)
                .eq(OrderInfo::getDeleted, 0)
                .last("LIMIT 100"));

        for (OrderInfo order : expireOrders) {
            try {
                cancelOrder(order.getId(), "订单超时自动取消");
            } catch (Exception e) {
                log.error("自动取消订单失败, orderId: {}", order.getId(), e);
            }
        }
    }

    private String generateOrderNo() {
        String dateStr = DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(6);
        return dateStr + random;
    }
}
