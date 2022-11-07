package com.atguigu.yygh.common.exception;

import com.atguigu.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: Wei
 * @date: 2022/10/18,11:16
 * @description: 全局异常处理
 */
@Slf4j
@ControllerAdvice // 以通知的形式处理异常
public class GlobalExceptionHandler {
    
    /**
     * 处理全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody // 响应Json数据
    public R error(Exception e) {
        e.printStackTrace();
        return R.error().message(e.getMessage());
    }
    
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public R error(ArithmeticException e){
        return R.error().message("发生了除0异常");
    }
    
    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public R error(YyghException e){
        log.error(e.getMsg()); // 将错误信息手动写入到本地磁盘
        e.printStackTrace();
        return R.error().code(e.getCode()).message(e.getMsg());
    }
    
}
