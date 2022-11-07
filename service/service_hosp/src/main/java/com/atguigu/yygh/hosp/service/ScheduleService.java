package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/25,16:18
 * @description:
 */
public interface ScheduleService {
    /**
     * 添加或修改排班信息
     *
     * @param newObjectMap
     */
    void saveSchedule(Map<String, Object> newObjectMap);
    
    /**
     * 带分页查询排班信息
     *
     * @param page
     * @param limit
     * @param scheduleQueryVo
     * @return
     */
    Page<Schedule> getPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);
    
    /**
     * 移除排班信息
     *
     * @param hoscode
     * @param hosScheduleId
     */
    void removeSchedule(String hoscode, String hosScheduleId);
    
    /**
     * 通过医院与科室编码,查询当前科室的所有排班信息,
     * 并按照日期分组,分别统计出总排班号数和剩余可用号数
     *
     * @param page    当前页
     * @param limit   每页总条数
     * @param hoscode 医院编码
     * @param depcode 科室编码
     * @return
     */
    Map<String, Object> getRulesSchedule(Long page, Long limit, String hoscode, String depcode);
    
    /**
     * 查询此医院科室某一工作日的排班详情信息
     *
     * @param hoscode  医院唯一标识
     * @param depcode  科室标识
     * @param workDate 工作日
     * @return
     */
    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);
    
    /**
     * 查询预约周期内的医院科室排班信息
     *
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    Map<String, Object> getBookingScheduleInfoOfCycle(Integer page, Integer limit, String hoscode, String depcode);
    
    /**
     * 根据科室id查询科室信息
     *
     * @param scheduleId
     * @return
     */
    Schedule getScheduleById(String scheduleId);
    
    
    /**
     * 根据排班id获取预约下单数据
     *
     * @param scheduleId
     * @return
     */
    ScheduleOrderVo getScheduleOrderVoByScheduleId(String scheduleId);
    
    /**
     * 更新排班数据
     *
     * @param schedule
     */
    void updateSchedule(Schedule schedule);
}
