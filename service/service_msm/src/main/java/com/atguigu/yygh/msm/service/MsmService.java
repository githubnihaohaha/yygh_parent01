package com.atguigu.yygh.msm.service;

/**
 * @author: Wei
 * @date: 2022/10/30,16:29
 * @description:
 */
public interface MsmService {
    /**
     * 向手机号发送一个随机验证码,并通过redis设置验证码的到期时间
     *
     * @param phone
     * @param code
     * @return 是否成功
     */
    boolean sendCode(String phone, String code);
}
