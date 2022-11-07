package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.model.base.BaseEntity;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author wei
 * @since 2022-11-02
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {
    
    @Autowired
    private DictFeignClient dictFeignClient;
    
    /**
     * 查询用户添加的所有就诊人信息
     *
     * @param userId
     * @return
     */
    @Override
    public List<Patient> getAllPatientByUserId(Long userId) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getUserId, userId);
        
        List<Patient> list = baseMapper.selectList(wrapper);
        list.stream().forEach(this::packPatient);
        
        return list;
    }
    
    /**
     * 查询就诊人详情
     *
     * @param id
     * @return
     */
    @Override
    public Patient getPatientById(Long id) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseEntity::getId, id);
        
        Patient patient = baseMapper.selectOne(wrapper);
        packPatient(patient);
        return patient;
        
    }
    
    
    /**
     * 远程调用封装数据
     *
     * @param patient
     */
    private void packPatient(Patient patient) {
        
        if (patient == null) {
            throw new YyghException(20001, "要封装的数据为空!");
        }
        
        String province = dictFeignClient.getName(patient.getProvinceCode());
        String city = dictFeignClient.getName(patient.getCityCode());
        String district = dictFeignClient.getName(patient.getDistrictCode());
        
        String cardType = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        
        Map<String, Object> param = patient.getParam();
        param.put("cityString", city);
        param.put("provinceString", province);
        param.put("districtString", district);
        param.put("certificatesTypeString", cardType);
        param.put("fullAddress", province + city + district + patient.getAddress());
        
    }
    
}
