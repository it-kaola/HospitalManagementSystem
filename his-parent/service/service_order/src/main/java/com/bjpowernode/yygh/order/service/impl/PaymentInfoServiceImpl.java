package com.bjpowernode.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjpowernode.yygh.common.helper.HttpRequestHelper;
import com.bjpowernode.yygh.enums.OrderStatusEnum;
import com.bjpowernode.yygh.enums.PaymentStatusEnum;
import com.bjpowernode.yygh.enums.PaymentTypeEnum;
import com.bjpowernode.yygh.hosp.client.HospitalFeignClient;
import com.bjpowernode.yygh.model.order.OrderInfo;
import com.bjpowernode.yygh.model.order.PaymentInfo;
import com.bjpowernode.yygh.order.dao.PaymentInfoDao;
import com.bjpowernode.yygh.order.service.OrderService;
import com.bjpowernode.yygh.order.service.PaymentInfoService;
import com.bjpowernode.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoDao, PaymentInfo> implements PaymentInfoService {

    @Resource
    private OrderService orderService;

    @Resource
    private HospitalFeignClient hospitalFeignClient;

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        // 根据订单id和支付类型，查询支付记录表中是否有该条记录，防止重复添加记录
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(queryWrapper);
        if(count > 0){
            return;
        }
        // 向支付记录表中添加记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        // subject表示支付信息
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);

    }


    // 更新支付记录和订单记录的支付状态
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {

        // 1 根据交易流水号得到支付记录 paymentInfo
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", out_trade_no);
        queryWrapper.eq("payment_type", PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);

        // 2 更新支付记录信息
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date()); // 支付时间
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);

        // 3 根据订单号得到订单信息
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());

        // 4 跟新订单信息
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);

        // 5 调用医院的接口，更新支付信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updatePayStatus");

    }


    // 根据订单编号和支付方式获取支付记录
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }
}
