package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/25,9:42
 * @description:
 */
public interface HospitalService {
    void saveHospital(Map<String, Object> newObjectMap);
    
    Hospital getHosp(String hoscode);
    
    Page<Hospital> getPageHosp(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);
    
    /**
     * 根据id查询医院的全部信息
     *
     * @param id
     * @return 该医院的详情信息
     */
    Map<String, Object> showHospInfo(String id);
    
    /**
     * 修改医院启用状态
     *
     * @param id
     * @param status
     */
    void updateStatus(String id, Integer status);
    
    /**
     * 根据输入的医院名称模糊查询
     *
     * @param hosname
     * @return
     */
    List<Hospital> getHospByHosnameLike(String hosname);
    
    /**
     * 通过hoscode查询医院医院信息及预约规则
     *
     * @param hoscode
     * @return
     */
    Map<String, Object> item(String hoscode);
}
