package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author wei
 * @since 2022-11-04
 */
public interface OrderInfoService extends IService<OrderInfo> {
    
    /**
     * 生成订单数据
     *
     * @param scheduleId
     * @param patientId
     * @return 订单号
     */
    Long createOrder(String scheduleId, Long patientId);
    
    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoByOrderId(Long orderId);
    
    /**
     * 取消订单
     *
     * @param orderId
     */
    void cancelAnOrderByOrderId(Long orderId);
    
    /**
     * 查询就诊日期为 dateTime 的就诊人并发送就诊提醒信息
     *
     * @param dateTime
     */
    void appointmentReminder(String dateTime);
    
    
    /**
     * 获取统计数量
     *
     * @param queryVo 按照医院或就诊日期区间
     * @return 日期&就诊人数List
     */
    Map<String, Object> getStatistics(OrderCountQueryVo queryVo);
}
