package com.atguigu.yygh.hosp.client;

import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: Wei
 * @date: 2022/11/4,19:11
 * @description:
 */
@FeignClient("service-hosp")
public interface HospFeignClient {
    
    /**
     * 生成订单使用,根据排班id获取预约下单数据
     *
     * @param scheduleId
     * @return ScheduleOrderVo
     */
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);
}
