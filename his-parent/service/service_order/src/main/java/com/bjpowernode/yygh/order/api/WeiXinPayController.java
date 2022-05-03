package com.bjpowernode.yygh.order.api;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.order.service.PaymentInfoService;
import com.bjpowernode.yygh.order.service.WeiXinPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "微信支付相关接口")
@RestController
@RequestMapping("/api/order/weixin")
public class WeiXinPayController {

    @Resource
    private WeiXinPayService weiXinPayService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @ApiOperation("根据订单ID生成微信支付二维码")
    @GetMapping("/createNative/{orderId}")
    public Result createNative(@PathVariable("orderId") Long orderId){
        Map<String, Object> map = weiXinPayService.createNative(orderId);
        return Result.ok(map);
    }

    @ApiOperation("查询支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable("orderId") Long orderId){
        // 调用微信接口实现支付状态的查询
        Map<String, String> resultMap = weiXinPayService.queryPayStatus(orderId);
        if(resultMap == null){
            return Result.fail().message("支付失败");
        }
        if("SUCCESS".equals(resultMap.get("trade_state"))){
            // 更新支付记录和订单记录的支付状态
            String out_trade_no = resultMap.get("out_trade_no"); // 交易流水号
            paymentInfoService.paySuccess(out_trade_no, resultMap);
            return Result.ok("支付成功");
        }
        return Result.ok().message("支付中");
    }
}
