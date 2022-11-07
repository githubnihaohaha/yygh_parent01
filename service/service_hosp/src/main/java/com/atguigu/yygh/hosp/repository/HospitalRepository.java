package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: Wei
 * @date: 2022/10/25,9:43
 * @description:
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    
    /**
     * 根据医院唯一标识查找医院
     *
     * @param hoscode
     * @return
     */
    Hospital findByHoscode(String hoscode);
    
    /**
     * 根据医院名称模糊查询
     *
     * @param hosname
     * @return
     */
    List<Hospital> getHospitalByHosnameLike(String hosname);
    
}
