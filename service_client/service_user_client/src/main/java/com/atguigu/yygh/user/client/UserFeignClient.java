package com.atguigu.yygh.user.client;

import com.atguigu.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: Wei
 * @date: 2022/11/4,19:07
 * @description:
 */
@FeignClient("service-user")
public interface UserFeignClient {
    
    /**
     * 远程调用,通过就诊人id获取就诊人信息
     *
     * @param patientId
     * @return
     */
    @GetMapping("/api/user/patient/inner/get/{patientId}")
    public Patient getPatientOrder(@PathVariable("patientId") Long patientId);
}
