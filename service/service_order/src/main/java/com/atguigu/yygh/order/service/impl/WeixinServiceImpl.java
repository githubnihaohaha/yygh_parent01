package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentInfoService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpClient;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;

import java.util.Collections;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/11/4,22:39
 * @description:
 */
@Service
public class WeixinServiceImpl implements WeixinService {
    
    @Autowired
    private OrderInfoService orderInfoService;
    
    @Autowired
    private PaymentInfoService paymentInfoService;
    
    @Autowired
    private RefundInfoService refundInfoService;
    
    /**
     * 获得微信支付二维码地址
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, Object> createNative(Long orderId) {
        // 1.根据订单id查询订单详情
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(orderId);
        
        // 2.向支付记录表内添加一条正在支付记录
        paymentInfoService.savePaymentInfo(orderInfo);
        
        // 3.调用微信固定接口,得到二维码地址等消息
        
        // 3.1 封装访问微信接口携带的参数
        Map<String, String> paramMap = new HashMap();
        //公众号appid
        paramMap.put("appid", ConstantPropertiesUtils.APPID);
        //商户号
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
        //随机字符串
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        Date reserveDate = orderInfo.getReserveDate();
        String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
        String body = reserveDateString + "就诊" + orderInfo.getDepname();
        //扫描后手机显示内容
        paramMap.put("body", body);
        //订单交易号
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        //支付金额
        paramMap.put("total_fee", "1");// 为了测试  0.01
        //客户端ip
        paramMap.put("spbill_create_ip", "127.0.0.1");
        //支付成功回调地址
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
        //二维码类型
        paramMap.put("trade_type", "NATIVE");
        
        // 3.2 使用 HttpClient 调用微信接口
        try {
            //3.2 使用httpclient方式发送post请求微信接口
            //设置请求路径
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置请求需要参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            //设置其他参数
            client.setHttps(true);
            //发送post请求
            client.post();
            
            //获取接口返回数据
            String xml = client.getContent();
            System.out.println("微信二维码返回：" + xml);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            
            //4 微信接口返回数据，把返回进行封装，返回
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url")); //微信二维码地址
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
        
    }
    
    /**
     * 查询订单的支付状态,并根据是否成功做进一步处理
     *
     * @param orderId
     * @return 用户的支付状态
     */
    @Override
    public String queryWxPayStatusByOrderId(Long orderId) {
        /*
         * 查询订单的微信支付状态,
         *   1.通过订单id查询订单信息
         *   2.封装数据后,调用微信接口查询
         *   3.根据返回的结果做进一步处理
         *       3.1 如果交易成功,更改该订单PaymentInfo&OrderInfo表的状态为已支付
         * */
        
        // 查询订单信息
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(orderId);
        
        // 封装数据
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        
        // 调用微信接口
        try {
            
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            
            // 获取微信接口返回结果
            String xmlContent = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlContent);
            
            String resultInfo;
            
            // 根据微信返回的支付结果做进一步处理
            if (resultMap.isEmpty()) {
                throw new YyghException(20001, "未查询到支付信息");
            }
            
            if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                // 成功,更改订单状态与支付记录表的状态
                paymentInfoService.paySuccess(resultMap);
                
                // 调用医院接口同步支付状态
                
                resultInfo = "支付成功";
            } else {
                resultInfo = "支付中";
            }
            
            return resultInfo;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        return null;
    }
    
    
    /**
     * 微信订单退款
     *
     * @param orderId
     * @return
     */
    @Override
    public boolean orderRefund(Long orderId) {
        /*
         * 1.通过订单id获取支付消息
         * 2.向退款表中添加一条退款记录
         * 3.判断订单的交易状态
         * 4.调用微信接口发起退款
         * 5.退款成功后修改支付状态为已退款
         * */
        PaymentInfo paymentInfo = paymentInfoService.getPaymentInfoByOrderId(orderId);
        if (paymentInfo == null) {
            throw new YyghException(20001, "支付记录不存在!");
        }
        
        // 向退款表中添加一条退款记录(退款中)
        RefundInfo refundInfo = refundInfoService.addRefundRecord(paymentInfo);
        
        // 若退款表中显示该笔订单已退款,直接返回 true
        if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus()) {
            return true;
        }
        
        
        // 开始调用微信接口发起退款
        try {
            Map<String, String> paramMap = new HashMap<>(8);
            paramMap.put("appid", ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id", paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //商户退款单号
            paramMap.put("total_fee", "1");// 为了测试 0.01
            paramMap.put("refund_fee", "1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);//设置xml格式参数
            client.setHttps(true);//支持https协议
            client.setCert(true); //使用证书
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);//设置证明密码
            client.post();
            
            // 获得微信的返回结果
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            
            if ((!resultMap.isEmpty()) && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                // 微信退款成功,更新退款表状态
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setUpdateTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setCallbackContent(JSON.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                
                return true;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}
