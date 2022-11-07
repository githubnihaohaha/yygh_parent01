package com.atguigu.yygh.hosp.receiver;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.rabbit.RabbitService;
import com.atguigu.yygh.rabbit.constant.MqConst;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
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
 * @date: 2022/11/4,21:39
 * @description:
 */
@Component
public class HospitalReceiver {
    
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private RabbitService rabbitService;
    
    /**
     * 接收order模块发送的消息,更新号源数量及向msm模块发送消息
     *
     * @param orderMqVo 排班id,号源数量相关数据及一个MsmVo实体
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void updateSchedule(OrderMqVo orderMqVo, Message message, Channel channel) {
        
        if (orderMqVo.getAvailableNumber() != null) {
            
            // 生成订单更新数量
            Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            scheduleService.updateSchedule(schedule);
            
        } else {
            
            // 用户取消订单,号源数 +1
            Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
            Integer availableNumber = schedule.getAvailableNumber() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.updateSchedule(schedule);
        }
        
        
        // 发送mq消息给Msm,短信发送
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (msmVo != null) {
            // 发送mq消息
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_DIRECT_MSM,
                    MqConst.ROUTING_MSM_ITEM,
                    msmVo);
        }
    }
}
