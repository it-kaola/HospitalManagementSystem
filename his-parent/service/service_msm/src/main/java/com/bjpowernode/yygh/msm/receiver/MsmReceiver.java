package com.bjpowernode.yygh.msm.receiver;

import com.bjpowernode.common.rabbit.constant.MqConst;
import com.bjpowernode.yygh.msm.service.MSMService;
import com.bjpowernode.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MsmReceiver {

    @Resource
    private MSMService msmService;

    /*当队列中有消息时触发该方法执行*/
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}))
    public void send(MsmVo msmVo, Message message, Channel channel) {
        msmService.sendMessageByMQ(msmVo);
    }
}

