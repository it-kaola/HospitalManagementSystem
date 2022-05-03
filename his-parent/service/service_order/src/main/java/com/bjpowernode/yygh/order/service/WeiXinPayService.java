package com.bjpowernode.yygh.order.service;

import java.util.Map;

public interface WeiXinPayService {

    // 根据订单ID生成微信支付二维码
    Map<String, Object> createNative(Long orderId);

    // 调用微信接口实现支付状态的查询
    Map<String, String> queryPayStatus(Long orderId);

    // 微信退款
    Boolean refund(Long orderId);
}
