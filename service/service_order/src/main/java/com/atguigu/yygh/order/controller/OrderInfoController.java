package com.atguigu.yygh.order.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author wei
 * @since 2022-11-04
 */
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {
    
    @Autowired
    private OrderInfoService orderInfoService;
    
    /**
     * 生成订单
     *
     * @param scheduleId
     * @param patientId
     * @return 订单号
     */
    @PostMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId,
                         @PathVariable Long patientId) {
        Long orderId = orderInfoService.createOrder(scheduleId, patientId);
        
        return R.ok().data("orderId", orderId);
    }
    
    /**
     * 根据订单id查询订单信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/auth/getOrders/{orderId}")
    public R getOrderInfo(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(orderId);
        return R.ok().data("orderInfo", orderInfo);
    }
    
    
    /**
     * 取消订单
     *
     * @param orderId
     * @return
     */
    @GetMapping("/auth/cancelOrder/{orderId}")
    public R cancelOrder(@PathVariable Long orderId) {
        orderInfoService.cancelAnOrderByOrderId(orderId);
        return R.ok();
    }
    
    
    /**
     * 获得统计数据
     *
     * @param queryVo 医院或挂号日期
     * @return 日期&挂号数量 List
     */
    @PostMapping("/getCountMap")
    public R getStatistics(@RequestBody OrderCountQueryVo queryVo) {
        Map<String, Object> resultMap = orderInfoService.getStatistics(queryVo);
        return R.ok().data(resultMap);
    }
}

