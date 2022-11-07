package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Wei
 * @date: 2022/10/25,16:18
 * @description:
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private HospitalService hospitalService;
    
    @Autowired
    private DepartmentService departmentService;
    
    /**
     * 获取分页排班信息
     *
     * @param page            当前页(从0开始)
     * @param limit           总页数
     * @param scheduleQueryVo 查询条件
     * @return
     */
    @Override
    public Page<Schedule> getPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        
        PageRequest pageRequest = PageRequest.of(page - 1, limit, sort);
        
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        
        Example<Schedule> example = Example.of(schedule, matcher);
        
        return scheduleRepository.findAll(example, pageRequest);
    }
    
    /**
     * 上传排班信息
     *
     * @param newObjectMap
     */
    @Override
    public void saveSchedule(Map<String, Object> newObjectMap) {
        
        String jsonString = JSONObject.toJSONString(newObjectMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);
        
        if (schedule == null) {
            throw new YyghException(20001, "失败!");
        }
        
        Schedule existsSchedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if (existsSchedule != null) {
            schedule.setId(existsSchedule.getId());
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(existsSchedule.getCreateTime());
        } else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
        }
        scheduleRepository.save(schedule);
        
    }
    
    /**
     * 删除排班
     *
     * @param hoscode
     * @param hosScheduleId
     */
    @Override
    public void removeSchedule(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }
    
    /**
     * 通过医院与科室编码,查询出当前科室的所有排班信息,
     * 并按照日期分组,分别统计出总排班号数和剩余可用号数
     *
     * @param page    当前页
     * @param limit   每页总条数
     * @param hoscode 医院编码
     * @param depcode 科室编码
     * @return
     */
    @Override
    public Map<String, Object> getRulesSchedule(Long page, Long limit, String hoscode, String depcode) {
        
        // 1.根据医院编号与科室编号查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        
        // 2.根据工作日 workDate 进行分组
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria), //查询条件
                Aggregation.group("workDate") //分组字段
                        .first("workDate") //必要的,它的含义是 select <workDate> from ....
                        .as("workDate") //非必要,查询结果的别名
                        // 统计出诊医生数量
                        .count().as("docCount")
                        // 统计号源数量
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //排序及分页
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        
        // 调用聚合查询
        AggregationResults<BookingScheduleRuleVo> aggResults =
                mongoTemplate.aggregate(aggregation,
                        Schedule.class,
                        BookingScheduleRuleVo.class);
        
        // 通过查询结果得到数据集合
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();
        
        /*
         * 3.遍历 bookingScheduleRuleVoList 集合,
         * 获取对象日期
         * 通过日期获得星期值,并将星期值封装到对象中
         *
         * */
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        
        /*
         * 4.查询日期分组的总数量
         * */
        Aggregation totalAggregation = Aggregation.newAggregation(Aggregation.match(criteria), Aggregation.group("workDate"));
        AggregationResults<BookingScheduleRuleVo> totalAggregate = mongoTemplate.aggregate(totalAggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResultsList = totalAggregate.getMappedResults();
        int total = mappedResultsList.size();
        
        /*
         * 5.封装数据到map
         * */
        Map<String, Object> map = new HashMap<>();
        map.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        map.put("total", total);
        
        //获取医院名称
        Hospital hosp = hospitalService.getHosp(hoscode);
        if (hosp != null) {
            Map<String, Object> baseMap = new HashMap<>();
            baseMap.put("hosname", hosp.getHosname());
            map.put("baseMap", baseMap);
        }
        return map;
    }
    
    
    /**
     * 查询此医院科室某一工作日的排班详情信息
     *
     * @param hoscode  医院唯一标识
     * @param depcode  科室标识
     * @param workDate 工作日
     * @return
     */
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        
        Date date = new DateTime(workDate).toDate();
        
        return scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);
    }
    
    /**
     * 查询预约周期内的医院科室排班信息
     *
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Map<String, Object> getBookingScheduleInfoOfCycle(Integer page, Integer limit, String hoscode, String depcode) {
        Hospital hosp = hospitalService.getHosp(hoscode);
        if (hosp == null) {
            throw new YyghException(20001, "失败");
        }
        
        /*
         * 1.获取要显示的的所有日期,
         *   每次获得一页(limit)数量的日期
         *
         * */
        BookingRule bookingRule = hosp.getBookingRule();
        IPage<Date> iPage = getListDate(page, limit, bookingRule);
        List<Date> dateListOfOnePage = iPage.getRecords();
        
        
        /*
         * 2.根据 医院编号 + 科室编号 + 所有日期 查询出所有的排班数据
         *
         * */
        // 封装查询条件
        Criteria criteria
                = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateListOfOnePage);
        
        // 封装聚合条件
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber"));
        
        // 查询
        AggregationResults<BookingScheduleRuleVo> aggregateResult
                = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        
        // 获得结果集合
        List<BookingScheduleRuleVo> mappedResults = aggregateResult.getMappedResults();
        
        // 将结果使用stream流转换为Map<日期,日期对应数据>
        Map<Date, BookingScheduleRuleVo> scheduleRuleVoMap
                = mappedResults.stream()
                .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                        bookingScheduleRuleVo -> bookingScheduleRuleVo));
        
        /*
         * 3.使用要显示数据的日期从map集合中获取数据
         *   如果为空则表示当天没有排班信息,做无号处理
         *
         * */
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        int size = dateListOfOnePage.size();
        for (int i = 0; i < size; i++) {
            // 获得周期内的某一天日期信息
            Date date = dateListOfOnePage.get(i);
            
            BookingScheduleRuleVo ruleVo = scheduleRuleVoMap.get(date);
            
            // 无号处理
            if (ruleVo == null) {
                ruleVo = new BookingScheduleRuleVo();
                ruleVo.setDocCount(0);
                ruleVo.setAvailableNumber(-1);
            }
            
            ruleVo.setWorkDate(date);
            ruleVo.setWorkDateMd(date);
            
            // 封装星期数据
            String dayOfWeek = getDayOfWeek(new DateTime(date));
            ruleVo.setDayOfWeek(dayOfWeek);
            
            
            // 如果是周期内的最后一个日期(最后一页最后一条数据),显示即将放号
            if (i == size - 1 && page == iPage.getPages()) {
                ruleVo.setStatus(1);
            } else {
                ruleVo.setStatus(0);
            }
            
            
            // 判断当天(第一个日期)是否已经超过了停止挂号的时间,如果是显示 停止挂号
            if (i == 0 && page == 1) {
                DateTime stopTime = getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    ruleVo.setStatus(-1);
                }
            }
            // 将处理完毕后的结果放到结果集中
            bookingScheduleRuleVoList.add(ruleVo);
        }
        // 封装 resultMap 返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("bookingScheduleList", bookingScheduleRuleVoList);
        resultMap.put("total", iPage.getTotal());
        
        // 其他基础数据
        Map<String, Object> baseMap = new HashMap<>();
        baseMap.put("hosname", hospitalService.getHosp(hoscode).getHosname());
        //科室
        Department department = departmentService.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        resultMap.put("baseMap", baseMap);
        
        return resultMap;
        
    }
    
    /**
     * 根据科室id查询科室信息
     *
     * @param scheduleId
     * @return
     */
    @Override
    public Schedule getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        packageSchedule(schedule);
        return schedule;
    }
    
    /**
     * 根据排班id获取预约下单数据
     *
     * @param scheduleId
     * @return
     */
    @Override
    public ScheduleOrderVo getScheduleOrderVoByScheduleId(String scheduleId) {
        
        // 创建用于最终数据封装的 Vo 值对象
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        
        // 通过排班id获取排班数据
        Schedule schedule = getScheduleById(scheduleId);
        if (schedule == null) {
            throw new YyghException(20001, "没有查询到排班数据");
        }
        
        // 获取医院基本信息
        Hospital hospital = hospitalService.getHosp(schedule.getHoscode());
        if (hospital == null) {
            throw new YyghException(20001, "医院信息不存在!");
        }
        
        // 获取预约规则信息
        BookingRule bookingRule = hospital.getBookingRule();
        if (bookingRule == null) {
            throw new YyghException(20001, "失败");
        }
        
        //封装相关数据到vo对象
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        String depname =
                departmentService.getDepartmentByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname();
        scheduleOrderVo.setDepname(depname);
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        
        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate())
                        .plusDays(quitDay)
                        .toDate(),
                bookingRule.getQuitTime());
        
        scheduleOrderVo.setQuitTime(quitTime.toDate());
        
        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());
        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        
        return scheduleOrderVo;
        
    }
    
    
    /**
     * 更新排班数据
     *
     * @param schedule
     */
    @Override
    public void updateSchedule(Schedule schedule) {
        
        scheduleRepository.save(schedule);
        
    }
    
    /**
     * 封装医院及科室名称
     *
     * @param schedule
     * @return
     */
    private Schedule packageSchedule(Schedule schedule) {
        if (schedule != null) {
            String hoscode = schedule.getHoscode();
            schedule.getParam().put("hosname", hospitalService.getHosp(hoscode).getHosname());
            
            Department department = departmentService.getDepartmentByHoscodeAndDepcode(hoscode, schedule.getDepcode());
            schedule.getParam().put("depname", department.getDepname());
            return schedule;
        }
        return null;
    }
    
    /**
     * 根据日期 + 预约周期,以分页的形式获取所有的显示日期
     *
     * @param page
     * @param limit
     * @param bookingRule 预约规则
     * @return Page对象
     */
    private IPage<Date> getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        
        /*
         * 1.判断当前时间是否过了放号的时间,如果超过了,预约周期 +1,多显示一天的数据
         *   1.1 releaseTime = 当天日期 + ReleaseTime 08:30
         *   1.2 获得预约周期
         *   1.3 判断是否已经
         *
         * */
        DateTime releaseTime = getDateTime(new Date(), bookingRule.getReleaseTime()); //获得dateTime
        Integer cycle = bookingRule.getCycle();
        if (releaseTime.isBeforeNow()) {
            cycle += 1;
        }
        
        
        /*
         * 2.得到预约周期内的所有日期,当前日期 + 预约周期
         *   2.1 获取一个预约周期内所有的日期
         *   2.2 将日期进行分页处理,每次返回每页数据
         *       将每一页要显示的日期分装到 IPage 对象中
         *
         * */
        List<Date> cycleDates = new ArrayList<>();
        
        for (Integer i = 0; i < cycle; i++) {
            
            DateTime currentDate = new DateTime().plusDays(i);
            
            String string = currentDate.toString("yyyy-MM-dd");
            Date date = new DateTime(string).toDate();
            
            cycleDates.add(date);
        }
        
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        if (end > cycleDates.size()) {
            end = cycleDates.size();
        }
        
        //每一页的日期集合
        ArrayList<Date> listOfAPage = new ArrayList<>();
        for (int i = start; i < end; i++) {
            listOfAPage.add(cycleDates.get(i));
        }
        
        
        IPage<Date> datePage
                = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, cycleDates.size());
        
        datePage.setRecords(listOfAPage);
        
        return datePage;
    }
    
    
    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }
    
    
    /**
     * 根据日期获取周几数据
     *
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
