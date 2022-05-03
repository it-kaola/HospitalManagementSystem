package com.bjpowernode.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjpowernode.yygh.enums.RefundStatusEnum;
import com.bjpowernode.yygh.model.order.PaymentInfo;
import com.bjpowernode.yygh.model.order.RefundInfo;
import com.bjpowernode.yygh.order.dao.RefundInfoDao;
import com.bjpowernode.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoDao, RefundInfo> implements RefundInfoService {

    @Resource
    private RefundInfoService refundInfoService;

    // 添加对应的退款记录
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        // 判断是否有重复数据添加
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("order_id", paymentInfo.getOrderId());
        queryWrapper.eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);
        if(refundInfo != null){
            return refundInfo;
        }
        // 添加记录
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        //paymentInfo.setSubject("test");
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refundInfo);

        return refundInfo;

    }
}
