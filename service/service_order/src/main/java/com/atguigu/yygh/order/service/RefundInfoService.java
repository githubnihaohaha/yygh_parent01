package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 退款信息表 服务类
 * </p>
 *
 * @author wei
 * @since 2022-11-06
 */
public interface RefundInfoService extends IService<RefundInfo> {
    
    /**
     * 添加退款记录
     *
     * @param paymentInfo 支付信息
     * @return
     */
    RefundInfo addRefundRecord(PaymentInfo paymentInfo);
}
