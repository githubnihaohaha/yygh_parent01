package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.order.service.WeixinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/11/4,22:31
 * @description:
 */
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {
    
    
    @Autowired
    private WeixinService weixinService;
    
    /**
     * 返回微信支付二维码地址等信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable("orderId") Long orderId) {
        
        Map<String, Object> map = weixinService.createNative(orderId);
        
        return R.ok().data(map);
    }
    
    
    /**
     * 查询订单的支付状态,并根据是否成功做进一步处理
     *
     * @param orderId
     * @return
     */
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryWxPayStatusByOrderId(@PathVariable Long orderId) {
        
        String wxPayStatus = weixinService.queryWxPayStatusByOrderId(orderId);
        
        return R.ok().message(wxPayStatus);
    }
    
    
}
