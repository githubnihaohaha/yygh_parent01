package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: Wei
 * @date: 2022/10/25,9:42
 * @description:
 */
@Service
public class HospitalServiceImpl implements HospitalService {
    
    @Autowired
    private HospitalRepository hospitalRepository;
    
    @Autowired
    private DictFeignClient dictFeignClient;
    
    @Override
    public Page<Hospital> getPageHosp(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        
        PageRequest pageAble = PageRequest.of(page - 1, limit, sort);
        
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        
        Example<Hospital> example = Example.of(hospital, matcher);
        
        Page<Hospital> pageModel = hospitalRepository.findAll(example, pageAble);
        
        /*
         * 获取查询到的List集合
         * 遍历每个Hospital对象
         * 获取每个对象的编号,远程调用根据编号获取名称,并将结果封装到Hospital的param属性中
         *
         * */
        pageModel.getContent().stream().forEach(this::packHospital);
        
        return pageModel;
        
    }
    
    
    /**
     * 通过远程调用,查询到医院登记及地区编号所对应的地址信息封装并返回
     *
     * @param hospital 等级与地区信息为编号的对象
     * @return 封装了等级及信息的 hospital 对象
     */
    private Hospital packHospital(Hospital hospital) {
        // 获取每个对象的编号
        String hostype = hospital.getHostype(); // 医院等级
        
        
        //远程调用通过编号获取对应名称
        String province = dictFeignClient.getName(hospital.getProvinceCode());//省
        String city = dictFeignClient.getName(hospital.getCityCode());//市
        String district = dictFeignClient.getName(hospital.getDistrictCode());//区
        
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hostype);
        
        //将数据存入 param 属性中
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", province + city + district + hospital.getAddress());
        
        return hospital;
    }
    
    @Override
    public Hospital getHosp(String hoscode) {
        return hospitalRepository.findByHoscode(hoscode);
    }
    
    @Override
    public void saveHospital(Map<String, Object> newObjectMap) {
        //使用 Json 工具类将 map转换为对象
        
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(newObjectMap), Hospital.class);
        
        /*
         * 医院对象内的编号是唯一索引不允许重复,
         * 判断前端传入的信息在数据库中是否存在
         *   存在,就给他setId后调用sava(),此时这个方法是更新信息
         *   不存在,直接调用save(),添加信息
         *
         * */
        Hospital isExcises = hospitalRepository.findByHoscode(hospital.getHoscode());
        if (isExcises != null) {
            hospital.setId(isExcises.getId());
            hospital.setUpdateTime(new Date());
        } else {
            hospital.setCreateTime(new Date());
        }
        hospitalRepository.save(hospital);
    }
    
    /**
     * 根据id查询医院的全部信息
     *
     * @param id
     * @return 该医院的详情信息
     */
    @Override
    public Map<String, Object> showHospInfo(String id) {
        
        Hospital hospital = packHospital(hospitalRepository.findById(id).get());
        
        Map<String, Object> map = new HashMap<>();
        map.put("hospital", hospital);
        map.put("bookingRule", hospital.getBookingRule());
        return map;
        
    }
    
    
    /**
     * 修改医院启用状态
     *
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(String id, Integer status) {
        if (status == 0 || status == 1) {
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
        }
    }
    
    /**
     * 根据输入的医院名称模糊查询
     *
     * @param hosname
     * @return
     */
    @Override
    public List<Hospital> getHospByHosnameLike(String hosname) {
        return hospitalRepository.getHospitalByHosnameLike(hosname);
    }
    
    /**
     * 通过hoscode查询医院医院信息及预约规则
     *
     * @param hoscode
     * @return
     */
    @Override
    public Map<String, Object> item(String hoscode) {
        
        Map<String, Object> map = new HashMap<>();
        
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        
        if (hospital == null) {
            return Collections.emptyMap();
        }
        BookingRule bookingRule = hospital.getBookingRule();
        map.put("bookingRule", bookingRule);
        hospital.setBookingRule(null);
        map.put("hospital", hospital);
        
        return map;
    }
}





















