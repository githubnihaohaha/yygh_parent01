package com.atguigu.yygh.order.receiver;

import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Wei
 * @date: 2022/11/6,22:52
 * @description:
 */
@Component
public class OrderReceiver {
    
    @Autowired
    private OrderInfoService orderInfoService;
    
    /**
     * 接收定时任务发送的信息,向预约时间为 dateTime 的就诊人发送就医提醒信息
     *
     * @param dateTime 就诊日期
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_8}
    ))
    public void appointmentReminder(String dateTime, Message message, Channel channel) {
        orderInfoService.appointmentReminder(dateTime);
    }
    
}
