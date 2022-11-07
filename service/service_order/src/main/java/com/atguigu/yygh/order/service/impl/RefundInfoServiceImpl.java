package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.RefundInfoMapper;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 退款信息表 服务实现类
 * </p>
 *
 * @author wei
 * @since 2022-11-06
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {
    
    /**
     * 添加退款记录
     *
     * @param paymentInfo 支付信息
     * @return
     */
    @Override
    public RefundInfo addRefundRecord(PaymentInfo paymentInfo) {
        /*
         * 1.查询退款表中是否已经有这个订单的记录,如果存在不需要添加
         * 2.不存在退款记录,向表中添加一条记录(退款中)并返回这个对象
         *
         * */
        LambdaQueryWrapper<RefundInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundInfo::getOrderId, paymentInfo.getOrderId());
        RefundInfo refundInfo = baseMapper.selectOne(wrapper);
        
        if (refundInfo != null) {
            return refundInfo;
        }
        
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setSubject(paymentInfo.getSubject());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        
        baseMapper.updateById(refundInfo);
        
        return refundInfo;
        
    }
}
