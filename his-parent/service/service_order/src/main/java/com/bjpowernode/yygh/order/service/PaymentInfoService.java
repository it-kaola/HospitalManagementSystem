package com.bjpowernode.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjpowernode.yygh.model.order.OrderInfo;
import com.bjpowernode.yygh.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentInfoService extends IService<PaymentInfo> {

    // 向支付记录表中添加一条记录
    void savePaymentInfo(OrderInfo orderInfo, Integer status);

    // 更新支付记录和订单记录的支付状态
    void paySuccess(String out_trade_no, Map<String, String> resultMap);

    // 根据订单编号和支付方式获取支付记录
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}
