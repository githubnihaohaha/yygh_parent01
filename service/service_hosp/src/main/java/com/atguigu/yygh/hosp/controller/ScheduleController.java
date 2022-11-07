package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/28,19:57
 * @description:
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    /**
     * 根据医院和科室编号,查询某一日期的排班信息
     *
     * @param page    当前显示页
     * @param limit   总页数
     * @param hoscode 医院编号
     * @param depcode 科室编号
     * @return
     */
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getScheduleRule(@PathVariable Long page,
                             @PathVariable Long limit,
                             @PathVariable String hoscode,
                             @PathVariable String depcode) {
        
        Map<String, Object> map = scheduleService.getRulesSchedule(page, limit, hoscode, depcode);
        
        return R.ok().data(map);
    }
    
    
    /**
     * 查询此医院科室某一工作日的排班详情信息
     *
     * @param hoscode  医院唯一标识
     * @param depcode  科室标识
     * @param workDate 工作日
     * @return
     */
    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail(@PathVariable String hoscode,
                               @PathVariable String depcode,
                               @PathVariable String workDate) {
        
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        
        return R.ok().data("list", list);
    }
    
    
}
