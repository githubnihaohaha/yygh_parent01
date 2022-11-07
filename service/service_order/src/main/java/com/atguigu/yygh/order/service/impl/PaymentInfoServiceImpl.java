package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 支付信息表 服务实现类
 * </p>
 *
 * @author wei
 * @since 2022-11-04
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
    
    @Autowired
    private OrderInfoService orderInfoService;
    
    /**
     * 添加支付记录
     *
     * @param orderInfo
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo) {
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderId, orderInfo.getId());
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }
        
        //添加
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(1);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        
        //支付状态 ：支付中
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + "|" + orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
        
    }
    
    /**
     * 用户付款成功,更新订单状态及支付记录状态为已支付
     *
     * @param resultMap 微信返回的数据集合
     */
    @Override
    public void paySuccess(Map<String, String> resultMap) {
        
        // 获得交易号
        String outTradeNo = resultMap.get("out_trade_no");
        
        
        // 修改订单支付状态为已支付
        LambdaQueryWrapper<OrderInfo> orderInfoWrapper = new LambdaQueryWrapper<>();
        orderInfoWrapper.eq(OrderInfo::getOutTradeNo, outTradeNo);
        OrderInfo orderInfo = orderInfoService.getOne(orderInfoWrapper);
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        
        orderInfoService.updateById(orderInfo);
        
        
        // 修改支付记录表的支付状态为已支付
        LambdaQueryWrapper<PaymentInfo> paymentInfoWrapper = new LambdaQueryWrapper<>();
        paymentInfoWrapper.eq(PaymentInfo::getOutTradeNo, outTradeNo);
        PaymentInfo paymentInfo = baseMapper.selectOne(paymentInfoWrapper);
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        
        // 向支付记录表中添加上此次订单的交易编码,它是微信退款的唯一标识
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        
        // 设置回调时间&信息
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(resultMap.toString());
        
        baseMapper.updateById(paymentInfo);
        
        
    }
    
    /**
     * 通过订单id查询支付信息
     *
     * @param orderId
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfoByOrderId(Long orderId) {
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderId, orderId);
        return baseMapper.selectOne(wrapper);
    }
}
