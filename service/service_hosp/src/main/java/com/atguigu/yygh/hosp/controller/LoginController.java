package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/19,10:28
 * @description:
 */
@RestController
@RequestMapping("/user/hosp")
public class LoginController {
    
    
    // login
    @PostMapping("/login")
    public R login(){
        return R.ok().data("token","admin-token");
    }
    
    // info
    @GetMapping("/info")
    public R info(){
        //{"code":20000,"data":{"roles":["admin"],
        // "introduction":"I am a super administrator",
        // "avatar":"https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif",
        // "name":"Super Admin"}}
        Map<String, Object> map = new HashMap<>();
        map.put("roles","admin");
        map.put("introduction","I am a super administrator");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        map.put("name","Super Admin");
        
        return R.ok().data(map);
    }
}
