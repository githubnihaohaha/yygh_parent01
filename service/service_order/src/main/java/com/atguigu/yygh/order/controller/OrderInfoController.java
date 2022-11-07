package com.atguigu.yygh.order.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
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
     * 这是一个用于测试流控规则的接口,添加了一个 hoscode 用于热点值判断
     *
     * @param hoscode
     * @param scheduleId
     * @param patientId
     * @return
     */
    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{hoscode}/{scheduleId}/{patientId}")
    @SentinelResource(value = "submitOrder", blockHandler = "submitOrderBlockHandler") // 指定服务降级兜底方法
    public R submitOrder(
            @ApiParam(name = "hoscode", value = "医院编号，限流使用", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable String scheduleId,
            @ApiParam(name = "patientId", value = "就诊人id", required = true)
            @PathVariable Long patientId) {
        //调用service方法
        //测试 返回订单号
        Long orderId = 1L;
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
    
    /**
     * 提交订单服务的兜底方法
     *
     * @param hoscode
     * @param scheduleId
     * @param patientId
     * @param e
     * @return
     */
    public R submitOrderBlockHandler(String hoscode,
                                     String scheduleId,
                                     Long patientId,
                                     BlockException e) { // 必须有 BlockException 参数
        
        return R.error().message("当前业务繁忙,请稍后重试!");
        
    }
    
    
    /**
     * 构造器,在初始化这个类时便调用这个方法进行流控规则的加载
     */
    public OrderInfoController() {
        initFlowQpsRule();
    }
    
    /**
     * 定义流控规则
     */
    private void initFlowQpsRule() {
        
        // 这个 List 用于存储所有的流控规则
        ArrayList<ParamFlowItem> rules = new ArrayList<>();
        
        // 新建一个流控规则
        ParamFlowRule paramFlowRule = new ParamFlowRule("submitOrder")
                // 限流第一个参数 submitOrder/10000/2/3
                .setParamIdx(0)
                // 设置单机阈值
                .setCount(5);
        
        // 针对热点值单独设置 QPS 流控规则,如：1000（北京协和医院）,可以通过数据库表一次性导入，目前为测试
        ParamFlowItem flowItem = new ParamFlowItem().setObject("10000") // 热点值
                .setClassType(String.class.getName()) // 热点值类型
                .setCount(1);// 热点值单机阈值
        
        // 可以将多个热点值放到 List 集合中
        rules.add(flowItem);
        
        // 将所有的热点值流控规则在 sentinel 中加载注册
        paramFlowRule.setParamFlowItemList(rules);
        ParamFlowRuleManager.loadRules(Collections.singletonList(paramFlowRule));
    }
    
}

