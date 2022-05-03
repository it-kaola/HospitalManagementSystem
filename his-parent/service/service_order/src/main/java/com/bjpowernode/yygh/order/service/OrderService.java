package com.bjpowernode.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bjpowernode.yygh.model.order.OrderInfo;
import com.bjpowernode.yygh.vo.order.OrderQueryVo;

public interface OrderService extends IService<OrderInfo> {

    // 生成挂号订单，订单内容包括就诊人信息和科室信息
    Long saveOrder(String scheduleId, Long patientId);

    // 根据订单的id查询订单的详细信息
    OrderInfo getOrderInfoDetail(String orderId);

    // 订单列表（条件查询带分页）
    IPage<OrderInfo> selectPage(Page<OrderInfo> page, OrderQueryVo orderQueryVo);

    // 取消预约
    boolean cancelOrder(Long orderId);
}
