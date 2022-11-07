package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

/**
 * @author: Wei
 * @date: 2022/10/25,16:19
 * @description:
 */
@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode,String scheduleId);
    
    List<Schedule> getScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);
}
