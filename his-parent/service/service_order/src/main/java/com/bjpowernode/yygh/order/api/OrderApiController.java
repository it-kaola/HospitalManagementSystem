package com.bjpowernode.yygh.order.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.enums.OrderStatusEnum;
import com.bjpowernode.yygh.model.order.OrderInfo;
import com.bjpowernode.yygh.order.service.OrderService;
import com.bjpowernode.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Resource
    private OrderService orderService;

    @ApiOperation("生成挂号订单，订单内容包括就诊人信息和科室信息")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(@PathVariable("scheduleId") String scheduleId, @PathVariable("patientId") Long patientId){
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    @ApiOperation("根据订单的id查询订单的详细信息")
    @GetMapping("/auth/getOrderInfoDetail/{orderId}")
    public Result getOrderInfoDetail(@PathVariable("orderId") String orderId){
        OrderInfo orderInfo = orderService.getOrderInfoDetail(orderId);
        return Result.ok(orderInfo);
    }

    @ApiOperation("订单列表（条件查询带分页）")
    @GetMapping("/auth/{pageNo}/{limit}")
    public Result UserList(@PathVariable("pageNo") Long pageNo, @PathVariable("limit") Long limit, OrderQueryVo orderQueryVo){
        Page<OrderInfo> page = new Page<>(pageNo, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(page, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }


    @ApiOperation("取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable("orderId") Long orderId){
        boolean isCancel = orderService.cancelOrder(orderId);
        return Result.ok(isCancel);
    }



}
