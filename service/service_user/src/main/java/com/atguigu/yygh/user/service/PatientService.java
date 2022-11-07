package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author wei
 * @since 2022-11-02
 */
public interface PatientService extends IService<Patient> {
    
    /**
     * 查询用户添加的所有就诊人信息
     *
     * @param userId
     * @return
     */
    List<Patient> getAllPatientByUserId(Long userId);
    
    /**
     * 查询就诊人详情
     *
     * @param id
     * @return
     */
    Patient getPatientById(Long id);
}
