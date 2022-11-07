package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/11/3,18:25
 * @description:
 */
@RestController
@RequestMapping("/admin/user")
public class UserController {
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 获取用户列表信息,条件分页查询
     *
     * @param page
     * @param limit
     * @param queryVo
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public R getPageList(@PathVariable Integer page,
                         @PathVariable Integer limit,
                         UserInfoQueryVo queryVo) {
        
        IPage<UserInfo> pageModel = userInfoService.getPageList(page, limit, queryVo);
        return R.ok().data("pageModel", pageModel);
        
    }
    
    
    /**
     * 通过用户id查询用户详情
     *
     * @param userId
     * @return
     */
    @GetMapping("/show/{userId}")
    public R userInfoDetail(@PathVariable Long userId) {
        Map<String, Object> map = userInfoService.getUerInfoDetailsByUserId(userId);
        return R.ok().data(map);
    }
    
    
    @GetMapping("/approval/{userId}/{authStatus}")
    public R approval(@PathVariable Long userId,@PathVariable Integer authStatus) {
        userInfoService.approval(userId,authStatus);
        return R.ok();
    }
}
