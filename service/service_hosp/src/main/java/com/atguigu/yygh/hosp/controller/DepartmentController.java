package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: Wei
 * @date: 2022/10/28,9:28
 * @description: 科室数据接口
 */
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    /**
     * 查询一个医院下的所有科室,按照大科室分组
     *
     * @param hoscode
     * @return
     */
    @GetMapping("/getDeptList/{hoscode}")
    public R getDepartmentByHoscode(@PathVariable String hoscode) {
        
        List<DepartmentVo> list = departmentService.getDepartmentTreeByHoscode(hoscode);
        
        return R.ok().data("list", list);
    }
    
}
