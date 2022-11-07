package com.atguigu.yygh.user.controller.api;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author wei
 * @since 2022-11-02
 */
@RestController
@RequestMapping("/api/user/patient")
public class PatientController {
    
    @Autowired
    private PatientService patientService;
    
    /**
     * 根据携带的token获取用户id,查询所有该用户的就诊人
     *
     * @param request
     * @return
     */
    @GetMapping("/auth/findAll")
    public R getPatientListByUserId(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.getAllPatientByUserId(userId);
        return R.ok().data("list", list);
    }
    
    
    /**
     * 查看某个就诊人详情信息
     *
     * @param id
     * @return
     */
    @GetMapping("/auth/get/{id}")
    public R getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return R.ok().data("patient", patient);
    }
    
    /**
     * 添加就诊人信息
     *
     * @param patient
     * @return
     */
    @PostMapping("/auth/save")
    public R savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }
    
    /**
     * 修改信息
     *
     * @param patient
     * @return
     */
    @PutMapping("/auth/update")
    public R updatePatientInfo(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return R.ok();
    }
    
    /**
     * 根据id移除就诊人
     *
     * @param id
     * @return
     */
    @DeleteMapping("/auth/remove/{id}")
    public R deletePatientById(@PathVariable Integer id) {
        patientService.removeById(id);
        return R.ok();
    }
    
    
    /**
     * 远程调用,通过就诊人id获取就诊人信息
     *
     * @param patientId
     * @return
     */
    @GetMapping("/inner/get/{patientId}")
    public Patient getPatientOrder(@PathVariable("patientId") Long patientId) {
        
        return patientService.getPatientById(patientId);
    }
}

