package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.hosp.client.HospFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentInfoService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpRequestHelper;
import com.atguigu.yygh.rabbit.RabbitService;
import com.atguigu.yygh.rabbit.constant.MqConst;
import com.atguigu.yygh.user.client.UserFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author wei
 * @since 2022-11-04
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    
    @Autowired
    private HospFeignClient hospFeignClient;
    
    @Autowired
    private UserFeignClient userFeignClient;
    
    @Autowired
    private RabbitService rabbitService;
    
    @Autowired
    private WeixinService weixinService;
    
    /**
     * 生成订单数据
     *
     * @param scheduleId
     * @param patientId
     * @return 订单号
     */
    @Override
    public Long createOrder(String scheduleId, Long patientId) {
        
        // 1.获取排班详情
        ScheduleOrderVo scheduleOrderVo = hospFeignClient.getScheduleOrderVo(scheduleId);
        
        // 2.获得就诊人详情信息
        Patient patient = userFeignClient.getPatientOrder(patientId);
        
        // 3.封装数据
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", scheduleOrderVo.getHoscode());
        paramMap.put("depcode", scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate", new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount", scheduleOrderVo.getAmount()); //挂号费用
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType", patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex", patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone", patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode", patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode", patient.getDistrictCode());
        paramMap.put("address", patient.getAddress());
        //联系人
        paramMap.put("contactsName", patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        paramMap.put("sign", "");
        
        // 4.调用医院接口进行挂号,如果挂号失败,不生产订单
        JSONObject result
                = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");
        if (result.getInteger("code") != 200) {
            throw new YyghException(20001, "挂号失败");
        } else {
            // 获得医院返回的数据
            JSONObject resultObj = result.getJSONObject("data");
            
            // 获取预约的唯一标识符(医院预约记录的主键
            String hosRecordId = resultObj.getString("hosRecordId");
            // 获取预约序号
            Integer number = resultObj.getInteger("number");
            // 取号时间
            String fetchTime = resultObj.getString("fetchTime");
            // 取号地址
            String fetchAddress = resultObj.getString("fetchAddress");
            
            /*
             * 将数据添加到订单表,生成订单
             * */
            OrderInfo orderInfo = new OrderInfo();
            // 设置排班数据
            BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
            
            // 生成交易号
            String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(1000);
            orderInfo.setOutTradeNo(outTradeNo);//订单交易号
            orderInfo.setScheduleId(scheduleId);//排班id
            orderInfo.setUserId(patient.getUserId()); //用户id
            
            // 就诊人信息
            orderInfo.setPatientId(patientId);
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            
            // 设置订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
            
            // 设置医院返回的数据
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            
            // 向订单表中添加本条订单数据
            baseMapper.insert(orderInfo);
            
            /*
             * 1.根据医院系统接口返回号数量，更新mongodb
             * 2.给挂号成功就诊人发送短信
             *   2.1 封装mq消息对象
             *   2.1 封装msmVo消息对象
             * */
            Integer reservedNumber = resultObj.getInteger("reservedNumber");
            Integer availableNumber = resultObj.getInteger("availableNumber");
            
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setScheduleId(scheduleId);
            
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(patient.getPhone());
            
            orderMqVo.setMsmVo(msmVo);
            
            // 发送 mq 消息
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_DIRECT_ORDER,
                    MqConst.ROUTING_ORDER,
                    orderMqVo);
            
            // 返回订单id
            return orderInfo.getId();
            
        }
    }
    
    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoByOrderId(Long orderId) {
        return this.packOrderInfo(baseMapper.selectById(orderId));
    }
    
    
    /**
     * 取消订单
     *
     * @param orderId
     */
    @Override
    public void cancelAnOrderByOrderId(Long orderId) {
        /*
         * 1.根据订单号查询出订单信息
         * 2.判断当前时间是否已经过了订单最晚取消时间
         * 3.通知医院取消预约
         * 4.判断用户是否已支付,如果已支付,调用微信接口退款,修改响应表的状态为已退款(订单表/退款表)
         * 5.发送 mq 消息更新号源数量
         *
         * */
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new YyghException(20001, "订单不存在");
        }
        
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()) {
            throw new YyghException(20001, "当前订单无法取消,已过最晚取消时间.");
        }
        
        // 调用医院接口,取消号源预约
        Map<String, Object> map = new HashMap<>();
        map.put("hoscode", orderInfo.getHoscode());
        map.put("hosRecordId", orderInfo.getHosRecordId());
        map.put("timestamp", HttpRequestHelper.getTimestamp());
        map.put("sign", "");
        JSONObject resultObj = HttpRequestHelper.sendRequest(map, "http://localhost:9998/order/updateCancelStatus");
        
        if (resultObj.getInteger("code") == 200) {
            
            // 判断是否已经支付
            Integer orderStatus = orderInfo.getOrderStatus();
            
            if (orderStatus.intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                // 调用微信退款
                boolean success = weixinService.orderRefund(orderId);
                if (!success) {
                    throw new YyghException(20001, "退款失败!");
                }
                
            } else {
                orderInfo.setOrderStatus(OrderStatusEnum.CANCEL.getStatus());
                orderInfo.setUpdateTime(new Date());
                baseMapper.updateById(orderInfo);
            }
            
            // 取消预约成功,发送 mq 消息,号源数量 +1
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            orderMqVo.setMsmVo(msmVo);
            
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,
                    MqConst.ROUTING_ORDER,
                    orderMqVo);
            
        } else {
            throw new YyghException(20001, "无法取消预约");
        }
    }
    
    /**
     * 查询就诊日期为 dateTime 的就诊人并发送就诊提醒信息
     *
     * @param dateTime
     */
    @Override
    public void appointmentReminder(String dateTime) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderStatus, OrderStatusEnum.PAID.getStatus());
        
        List<OrderInfo> orderInfos = baseMapper.selectList(wrapper);
        
        for (OrderInfo orderInfo : orderInfos) {
            String patientPhone = orderInfo.getPatientPhone();
            
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(patientPhone);
            
            // 发送 mq 信息,向就诊人发送预约提醒
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
        
    }
    
    /**
     * 获取统计数量
     *
     * @param queryVo 按照医院或就诊日期区间
     * @return 日期&就诊人数List
     */
    @Override
    public Map<String, Object> getStatistics(OrderCountQueryVo queryVo) {
        List<OrderCountVo> countVos = baseMapper.selectOrderCount(queryVo);
        
        List<String> reserveList
                = countVos.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        
        List<Integer> countList
                = countVos.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        
        Map<String, Object> map = new HashMap<>();
        map.put("dateList", reserveList);
        map.put("countList", countList);
        
        return map;
    }
    
    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString",
                OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
