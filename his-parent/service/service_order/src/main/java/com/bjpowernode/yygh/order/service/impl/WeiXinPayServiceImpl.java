package com.bjpowernode.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bjpowernode.yygh.enums.PaymentTypeEnum;
import com.bjpowernode.yygh.enums.RefundStatusEnum;
import com.bjpowernode.yygh.model.order.OrderInfo;
import com.bjpowernode.yygh.model.order.PaymentInfo;
import com.bjpowernode.yygh.model.order.RefundInfo;
import com.bjpowernode.yygh.order.service.OrderService;
import com.bjpowernode.yygh.order.service.PaymentInfoService;
import com.bjpowernode.yygh.order.service.RefundInfoService;
import com.bjpowernode.yygh.order.service.WeiXinPayService;
import com.bjpowernode.yygh.order.utils.ConstantPropertiesUtils;
import com.bjpowernode.yygh.order.utils.HttpClient;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    @Resource
    OrderService orderService;

    @Resource
    PaymentInfoService paymentInfoService;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    RefundInfoService refundInfoService;


    // 根据订单ID生成微信支付二维码
    @Override
    public Map<String, Object> createNative(Long orderId) {


        Map<String, Object> map = (Map<String, Object>) redisTemplate.opsForValue().get(orderId.toString());
        if(map != null){
            return map;
        }

        try {
            // 1 根据订单编号获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);
            // 2 向支付记录表中添加一条记录
            paymentInfoService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus()); // PaymentTypeEnum.WEIXIN.getStatus()表示支付方式，"2"表示微信支付
            // 3 设置相关参数，调用微信提供的生成二维码的接口，需要将参数转成xml格式，传递参数时需要使用到商户key
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = orderInfo.getReserveDate() + "就诊"+ orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1"); // 表示支付的金额，为0.01元
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            // 4 调用微信生成支付二维码接口
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            // 5 设置xml参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true); // 允许https协议
            client.post();
            // 6 返回相关数据
            String xmlContent = client.getContent();
            // 将xml格式的数据转成map集合
            Map<String, String> responseMap = WXPayUtil.xmlToMap(xmlContent);
            System.out.println("responseMap：" + responseMap);
            // 7 封装返回结果集
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("orderId", orderId);
            resultMap.put("totalFee", orderInfo.getAmount());
            resultMap.put("resultCode", responseMap.get("result_code"));
            resultMap.put("codeUrl", responseMap.get("code_url")); // 支付二维码的地址

            // 向redis存放数据，有效时间为两个小时
            if(responseMap.get("result_code") != null){
                redisTemplate.opsForValue().set(orderId.toString(), resultMap, 120, TimeUnit.MINUTES);
            }

            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    // 调用微信接口实现支付状态的查询
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try{
            // 1 根据orderId获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);

            // 2 用map集合封装提交参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机生成的字符串

            // 3 设置请求内容，调用接口
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();

            // 4 得到并返回相应数据
            String xmlContent = client.getContent();
            Map<String, String> responseMap = WXPayUtil.xmlToMap(xmlContent);
            System.out.println("调用微信提供的接口，查询支付状态的结果：" + responseMap);
            return responseMap;

        }catch (Exception error){
            return null;
        }
    }


    // 微信退款
    @Override
    public Boolean refund(Long orderId) {

        try {
            // 获取支付记录
            PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            // 添加信息到退款记录表中
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                // 如果退款记录的退款状态为"已退款"则直接返回true
                return true;
            }
            // 调用微信的接口，实现退款
            Map<String,String> paramMap = new HashMap<>(8);
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
            // paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            // paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            // 设置调用接口内容
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            // 设置证书的相关信息
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();

            String xmlContent = client.getContent();
            Map<String, String> responseMap = WXPayUtil.xmlToMap(xmlContent);
            System.out.println("退款的responseMap：" + responseMap);

            if (null != responseMap && WXPayConstants.SUCCESS.equalsIgnoreCase(responseMap.get("result_code"))) {
                // 更新退款记录
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(responseMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(responseMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


}
