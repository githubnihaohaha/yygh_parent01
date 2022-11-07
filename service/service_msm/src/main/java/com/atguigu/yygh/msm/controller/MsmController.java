package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @author: Wei
 * @date: 2022/10/30,16:29
 * @description:
 */
@RestController
@RequestMapping("/api/msm")
public class MsmController {
    
    @Autowired
    private MsmService msmService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 发送验证码接口
     *
     * @param phone
     * @return
     */
    @GetMapping("/send/{phone}")
    public R sendCode(@PathVariable String phone) {
        
        // redis已经存在这个验证码,不用重新发送
        String existsCode = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(existsCode)) return R.ok();
        
        
        String code = RandomUtil.getFourBitRandom();
        
        boolean isSend = msmService.sendCode(phone, code);
        
        if (isSend) {
            //在redis中添加验证码并设置过期时长
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return R.ok();
        } else {
            return R.error();
        }
    }
}
