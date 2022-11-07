package com.atguigu.yygh.user.controller.api;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/30,11:37
 * @description:
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 登录验证接口
     *
     * @param loginVo
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo) {
        Map<String, Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }
    
    
    /**
     * 用户认证接口
     *
     * @param userAuthVo
     * @param request
     * @return
     */
    @PostMapping("/auth/userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        userInfoService.userAuth(AuthContextHolder.getUserId(request), userAuthVo);
        return R.ok();
    }
    
    
    @GetMapping("/auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return R.ok().data("userInfo", userInfo);
    }
}
