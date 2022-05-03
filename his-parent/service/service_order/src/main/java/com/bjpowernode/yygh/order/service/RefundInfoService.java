package com.bjpowernode.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjpowernode.yygh.model.order.PaymentInfo;
import com.bjpowernode.yygh.model.order.RefundInfo;

public interface RefundInfoService extends IService<RefundInfo> {

    // 添加对应的退款记录
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
