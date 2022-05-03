package com.bjpowernode.yygh.hosp.receiver;

import com.bjpowernode.common.rabbit.constant.MqConst;
import com.bjpowernode.common.rabbit.service.RabbitService;
import com.bjpowernode.yygh.hosp.service.ScheduleService;
import com.bjpowernode.yygh.model.hosp.Schedule;
import com.bjpowernode.yygh.vo.msm.MsmVo;
import com.bjpowernode.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class HospitalReceiver {

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private RabbitService rabbitService;



    /*当队列中有消息时触发该方法执行*/
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        if(null != orderMqVo.getAvailableNumber()){
            //下单成功更新预约数
            Schedule schedule = scheduleService.getScheduleByScheduleId(orderMqVo.getScheduleId());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            scheduleService.updateSchedule(schedule);
        }else{
            // 退款成功跟新预约数
            Schedule schedule = scheduleService.getScheduleByScheduleId(orderMqVo.getScheduleId());
            int availableNumber = orderMqVo.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.updateSchedule(schedule);
        }


        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}

