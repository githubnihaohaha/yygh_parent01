package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 支付信息表 服务类
 * </p>
 *
 * @author wei
 * @since 2022-11-04
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    
    /**
     * 添加支付记录
     *
     * @param orderInfo
     */
    void savePaymentInfo(OrderInfo orderInfo);
    
    /**
     * 用户付款成功,更新订单状态及支付记录状态为已支付
     *
     * @param resultMap 微信返回的数据集合
     */
    void paySuccess(Map<String, String> resultMap);
    
    /**
     * 通过订单id查询支付信息
     *
     * @param orderId
     * @return
     */
    PaymentInfo getPaymentInfoByOrderId(Long orderId);
}
