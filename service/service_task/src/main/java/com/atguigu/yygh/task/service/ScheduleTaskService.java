package com.atguigu.yygh.task.service;

import com.atguigu.yygh.rabbit.RabbitService;
import com.atguigu.yygh.rabbit.constant.MqConst;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author: Wei
 * @date: 2022/11/6,22:28
 * @description: 定时任务模块
 */
@Service
@EnableScheduling
public class ScheduleTaskService {
    
    @Autowired
    private RabbitService rabbitService;
    
    /**
     * 模拟预约就诊提示
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = "0 0 20 * * ?")
    public void reminder() {
        DateTime dateTime = new DateTime().plusDays(1);
        String date = dateTime.toString();
        
        // 发送 Mq 信息
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,
                MqConst.ROUTING_TASK_8,
                date);
    }
}
