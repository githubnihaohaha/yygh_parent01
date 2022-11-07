package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/30,12:46
 * @description:
 */

public interface UserInfoService extends IService<UserInfo> {
    
    /**
     * 验证用户登录信息
     *
     * @param loginVo
     * @return name, token
     */
    Map<String, Object> login(LoginVo loginVo);
    
    /**
     * 通过openid查询获取用户信息
     *
     * @param openid
     * @return
     */
    UserInfo getUserInfoByOpenId(String openid);
    
    /**
     * 用户认证
     *
     * @param userId
     * @param userAuthVo
     */
    void userAuth(Long userId, UserAuthVo userAuthVo);
    
    /**
     * 条件分页查询用户信息
     *
     * @param page
     * @param limit
     * @param queryVo
     * @return
     */
    IPage<UserInfo> getPageList(Integer page, Integer limit, UserInfoQueryVo queryVo);
    
    /**
     * 通过用户id查询用户详细信息(包含就诊人信息)
     *
     * @param userId
     * @return
     */
    Map<String, Object> getUerInfoDetailsByUserId(Long userId);
    
    /**
     * 用户认证
     *
     * @param userId
     * @param authStatus
     */
    void approval(Long userId, Integer authStatus);
}
