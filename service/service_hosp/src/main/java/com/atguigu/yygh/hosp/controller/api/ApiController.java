package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.utils.MD5;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/25,9:40
 * @description:
 */
@Api(tags = "医院管理数据API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {
    
    @Autowired
    private HospitalService hospitalService;
    
    @Autowired
    private HospitalSetService hospitalSetService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private ScheduleService scheduleService;
    
    //============================== 排班信息 ====================================
    @ApiOperation(value = "删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        checkSignkey(newObjectMap);
        String hoscode = (String) newObjectMap.get("hoscode");
        String hosScheduleId = (String) newObjectMap.get("hosScheduleId");
        
        scheduleService.removeSchedule(hoscode,hosScheduleId);
        
        return Result.ok();
    }
    
    
    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        checkSignkey(newObjectMap);
        String hoscode = (String) newObjectMap.get("hoscode");
        String depcode = (String) newObjectMap.get("depcode");
    
        int page = StringUtils.isEmpty(newObjectMap.get("page")) ? 1 : Integer.parseInt((String) newObjectMap.get("page"));
        int limit = StringUtils.isEmpty(newObjectMap.get("limit")) ? 10 : Integer.parseInt((String) newObjectMap.get("limit"));
    
        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        
        Page<Schedule> pageModel = scheduleService.getPage(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }
    
    
    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        checkSignkey(newObjectMap);
        
        scheduleService.saveSchedule(newObjectMap);
        
        return Result.ok();
    }
    
    
    // ================================= 科室 =====================================
    @ApiOperation(value = "获取分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        checkSignkey(newObjectMap);
        
        String hoscode = (String) newObjectMap.get("hoscode");
        String depcode = (String) newObjectMap.get("depcode");
        
        int page = StringUtils.isEmpty(newObjectMap.get("page")) ? 1 : Integer.parseInt((String) newObjectMap.get("page"));
        int limit = StringUtils.isEmpty(newObjectMap.get("limit")) ? 10 : Integer.parseInt((String) newObjectMap.get("limit"));
        
        DepartmentQueryVo queryVo = new DepartmentQueryVo();
        queryVo.setHoscode(hoscode);
        queryVo.setDepcode(depcode);
        
        Page<Department> pageModel = departmentService.getPage(page, limit, queryVo);
        return Result.ok(pageModel);
    }
    
    
    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        
        checkSignkey(newObjectMap);
        
        departmentService.saveDept(newObjectMap);
        return Result.ok();
    }
    
    
    // ================================= 医院 ==============================
    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result<Hospital> hospital(HttpServletRequest request) {
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) newObjectMap.get("hoscode");
        
        // 参数校验
        if (StringUtils.isEmpty(hoscode)) {
            throw new YyghException(20001, "失败");
        }
        checkSignkey(newObjectMap);
        
        Hospital hospital = hospitalService.getHosp(hoscode);
        return Result.ok(hospital);
    }
    
    
    @ApiOperation("添加医院信息")
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        //外部传入的数据在系统中并没有对应的实体类,无法使用@RequestBody,用request可以获得所有的请求信息
        
        //获取提交的参数,并封装到集合内.String[] 数组是为了接收同一个key多个value的情况,例如前端的复选框值等
        Map<String, String[]> parameterMap = request.getParameterMap();
        
        // 为了操作方便,将String[] 转为Obj
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(parameterMap);
        
        
        checkSignkey(newObjectMap);
        
        // logo图片在传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String) newObjectMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        newObjectMap.put("logoData", logoData);
        
        hospitalService.saveHospital(newObjectMap);
        return Result.ok();
    }
    
    
    //============================ 签名验证 ====================================
    /*
     * 添加签名校验
     *   获取医院模拟系统传递的signKey(进行MD5加密)
     *   查询当前医院在平台保存的signKey
     *   将他们做对比,若一致即验证成功
     * */
    private void checkSignkey(Map<String, Object> newObjectMap) {
        String signHosp = (String) newObjectMap.get("sign");
        String hoscode = (String) newObjectMap.get("hoscode");
        
        String signPlatform = hospitalSetService.getHopSignKeyByHoscode(hoscode);
        
        String signPlatformMd5 = MD5.encrypt(signPlatform);
        if (!signPlatformMd5.equals(signHosp)) {
            throw new YyghException(20001, "签名认证失败!");
        }
    }
}























