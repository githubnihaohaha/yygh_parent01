package com.atguigu.yygh.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/18,9:11
 * @description: 统一返回结果类
 */
@Data
public class R {
    
    private boolean success; // 是否成功
    
    private Integer code; // 响应码
    
    private String message; // 返回信息
    
    private Map<String,Object> data = new HashMap<>();
    
    // 构造器私有化
    private R() {}
    
    // 提供外部调用的方法
    public static R ok(){
        R r = new R();
        r.setCode(ResultCode.SUCCESS);
        r.setMessage("成功");
        r.setSuccess(true);
        return r;
    }
    
    public static R error(){
        R r = new R();
        r.setCode(ResultCode.ERROR);
        r.setMessage("失败");
        r.setSuccess(false);
        return r;
    }
    
    public R success(Boolean success){
        this.setSuccess(success);
        return this;
    }
    public R message(String message){
        this.setMessage(message);
        return this;
    }
    public R code(Integer code){
        this.setCode(code);
        return this;
    }
    public R data(String key, Object value){
        this.data.put(key, value);
        return this;
    }
    public R data(Map<String, Object> map){
        this.setData(map);
        return this;
    }
}
