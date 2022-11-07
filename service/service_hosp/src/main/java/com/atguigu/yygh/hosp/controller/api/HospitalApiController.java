package com.atguigu.yygh.hosp.controller.api;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;


/**
 * @author: Wei
 * @date: 2022/10/30,9:37
 * @description:
 */

@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {
    
    @Autowired
    private HospitalService hospitalService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private ScheduleService scheduleService;
    
    
    /**
     * 分页获取预约周期内的科室排班信息
     *
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    @GetMapping("/auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R findScheduleList(@PathVariable Integer page,
                              @PathVariable Integer limit,
                              @PathVariable String hoscode,
                              @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getBookingScheduleInfoOfCycle(page, limit, hoscode, depcode);
        
        return R.ok().data(map);
    }
    
    
    /**
     * 获取该科室某一天下的排班详情
     *
     * @param hoscode
     * @param depcode
     * @param workDate 日期
     * @return
     */
    @GetMapping("/auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(
            @PathVariable String hoscode,
            @PathVariable String depcode,
            @PathVariable String workDate) {
        
        List<Schedule> scheduleDetail = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return R.ok().data("scheduleList", scheduleDetail);
        
    }
    
    
    /**
     * 获取科室详细信息
     *
     * @param scheduleId
     * @return
     */
    @GetMapping("/getSchedule/{scheduleId}")
    public R getScheduleById(@PathVariable String scheduleId) {
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return R.ok().data("schedule", schedule);
    }
    
    
    /**
     * 查询所有符合条件的医院信息分页后返回
     *
     * @param page
     * @param limit
     * @param hospitalQueryVo
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public R getHospPages(@PathVariable Integer page,
                          @PathVariable Integer limit,
                          HospitalQueryVo hospitalQueryVo) {
        
        Page<Hospital> pageModel = hospitalService.getPageHosp(page, limit, hospitalQueryVo);
        return R.ok().data("pages", pageModel);
    }
    
    /**
     * 通过医院名称模糊查询
     *
     * @param hosname
     * @return
     */
    @GetMapping("/findByHosname/{hosname}")
    public R findHospByHosname(@PathVariable String hosname) {
        List<Hospital> hospitalList = hospitalService.getHospByHosnameLike(hosname);
        return R.ok().data("list", hospitalList);
    }
    
    
    /**
     * 根据医院唯一标识查询医院信息及其预约规则
     *
     * @param hoscode
     * @return hospital, bookingRule
     */
    @GetMapping("/{hoscode}")
    public R showHospInfoByHoscode(@PathVariable String hoscode) {
        Map<String, Object> map = hospitalService.item(hoscode);
        return R.ok().data(map);
    }
    
    
    /**
     * 查询科室信息
     *
     * @param hoscode
     * @return
     */
    @GetMapping("/department/{hoscode}")
    public R findDepartmentByHoscode(@PathVariable String hoscode) {
        List<DepartmentVo> deptTree = departmentService.getDepartmentTreeByHoscode(hoscode);
        return R.ok().data("list", deptTree);
    }
    
    
    /**
     * 生成订单使用,根据排班id获取预约下单数据
     *
     * @param scheduleId
     * @return ScheduleOrderVo
     */
    @GetMapping("/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVoByScheduleId(scheduleId);
    }
}
