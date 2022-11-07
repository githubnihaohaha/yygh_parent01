package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/25,15:21
 * @description:
 */
public interface DepartmentService {
    /**
     * 添加科室信息
     *
     * @param objectMap
     */
    void saveDept(Map<String, Object> objectMap);
    
    /**
     * 分页获得科室信息
     *
     * @param page
     * @param limit
     * @param queryVo
     * @return
     */
    Page<Department> getPage(int page, int limit, DepartmentQueryVo queryVo);
    
    
    /**
     * 通过医院的唯一标识获取科室信息,将结果封装到Vo对象中并以一个大科室对象内包含多个小科室的方式返回
     *
     * @param hoscode 医院唯一标识
     * @return
     */
    List<DepartmentVo> getDepartmentTreeByHoscode(String hoscode);
    
    /**
     * 通过医院编号与科室编号获得科室信息
     *
     * @param hoscode
     * @param depcode
     * @return
     */
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
