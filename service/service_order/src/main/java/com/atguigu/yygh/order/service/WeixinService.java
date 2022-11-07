package com.atguigu.yygh.order.service;

import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/11/4,22:39
 * @description:
 */
public interface WeixinService {
    
    /**
     * 获得微信支付二维码地址
     *
     * @param orderId
     * @return
     */
    Map<String, Object> createNative(Long orderId);
    
    /**
     * 查询订单的支付状态,并根据是否成功做进一步处理
     *
     * @param orderId
     * @return
     */
    String queryWxPayStatusByOrderId(Long orderId);
    
    /**
     * 微信订单退款
     *
     * @param orderId
     * @return
     */
    boolean orderRefund(Long orderId);
}
