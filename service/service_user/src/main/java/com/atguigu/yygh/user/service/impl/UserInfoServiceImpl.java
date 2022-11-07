package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/30,12:47
 * @description:
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    
    @Autowired
    private PatientService patientService;
    
    /**
     * 验证用户登录信息
     *
     * @param loginVo
     * @return name, token
     */
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        
        if (loginVo == null) {
            throw new YyghException(20001, "失败");
        }
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(20001, "数据为空!");
        }
        // 验证码验证
        String mobileCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(mobileCode)) {
            throw new YyghException(20001, "验证码错误!");
        }
        
        
        //判断是否需要绑定手机号
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            
            UserInfo userInfo = bindingPhone(loginVo);
            return getToken(userInfo);
            
        } else {
            
            //通过手机号来查询数据库是否有这个对象
            LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInfo::getPhone, phone);
            UserInfo userInfo = baseMapper.selectOne(wrapper);
            
            //数据库中不存在这个用户,直接注册
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setName("");
                userInfo.setStatus(1);
                userInfo.setCreateTime(new Date());
                baseMapper.insert(userInfo);
            }
            
            if (userInfo.getStatus() == 0) {
                throw new YyghException(20001, "用户状态异常");
            }
            
            return getToken(userInfo);
        }
    }
    
    /**
     * 绑定手机号的方法
     *
     * @param loginVo
     */
    private UserInfo bindingPhone(LoginVo loginVo) {
        
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(UserInfo::getOpenid, loginVo.getOpenid());
        UserInfo userWx = baseMapper.selectOne(wrapper);
        
        wrapper.clear();
        
        String phone = loginVo.getPhone();
        wrapper.eq(UserInfo::getPhone, phone);
        UserInfo userByPhone = baseMapper.selectOne(wrapper);
        
        //查询用户要绑定的手机号,如果手机号是空,则绑定成功
        if (userByPhone == null) {
            
            userWx.setPhone(phone);
            baseMapper.updateById(userWx);
            
            return userWx;
        } else {
            
            // 用户要绑定的手机号账户已经存在,将微信的信息提取到手机账户
            userByPhone.setOpenid(userWx.getOpenid());
            userByPhone.setNickName(userWx.getNickName());
            
            baseMapper.updateById(userByPhone);
            
            baseMapper.deleteById(userWx.getId());
            
            return userByPhone;
        }
    }
    
    
    /**
     * 返回token
     *
     * @param userInfo
     * @return
     */
    private Map<String, Object> getToken(UserInfo userInfo) {
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        String token = JwtHelper.createToken(userInfo.getId(), name);
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("name", name);
        tokenMap.put("token", token);
        
        return tokenMap;
    }
    
    
    /**
     * 通过openid查询获取用户信息
     *
     * @param openid
     * @return
     */
    @Override
    public UserInfo getUserInfoByOpenId(String openid) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getOpenid, openid);
        return baseMapper.selectOne(wrapper);
    }
    
    /**
     * 用户认证
     *
     * @param userId
     * @param userAuthVo
     */
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = baseMapper.selectById(userId);
        
        if (userInfo != null) {
            userInfo.setName(userAuthVo.getName());
            
            userInfo.setCertificatesType(userAuthVo.getCertificatesType());
            userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
            userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
            userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
            
            baseMapper.updateById(userInfo);
        }
        
    }
    
    /**
     * 条件分页查询用户信息
     *
     * @param page
     * @param limit
     * @param queryVo
     * @return
     */
    @Override
    public IPage<UserInfo> getPageList(Integer page, Integer limit, UserInfoQueryVo queryVo) {
        Page<UserInfo> infoPage = new Page<>(page, limit);
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (!StringUtils.isEmpty(queryVo.getKeyword())) {
            wrapper.like(UserInfo::getName, queryVo.getKeyword());
        }
        if (!StringUtils.isEmpty(queryVo.getCreateTimeBegin())) {
            wrapper.gt(UserInfo::getCreateTime, queryVo.getCreateTimeBegin());
        }
        if (!StringUtils.isEmpty(queryVo.getCreateTimeEnd())) {
            wrapper.lt(UserInfo::getCreateTime, queryVo.getCreateTimeEnd());
        }
        if (!StringUtils.isEmpty(queryVo.getStatus())) {
            wrapper.lt(UserInfo::getStatus, queryVo.getStatus());
        }
        if (!StringUtils.isEmpty(queryVo.getAuthStatus())) {
            wrapper.lt(UserInfo::getAuthStatus, queryVo.getAuthStatus());
        }
        
        Page<UserInfo> userInfoPage = baseMapper.selectPage(infoPage, wrapper);
        
        userInfoPage.getRecords().stream().forEach(this::packageUserInfo);
        
        return userInfoPage;
    }
    
    /**
     * 通过用户id查询用户详细信息(包含就诊人信息)
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> getUerInfoDetailsByUserId(Long userId) {
        UserInfo userInfo = packageUserInfo(baseMapper.selectById(userId));
        List<Patient> patientList = patientService.getAllPatientByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("userInfo", userInfo);
        map.put("patientList", patientList);
        return map;
    }
    
    /**
     * 用户认证
     *
     * @param userId
     * @param authStatus
     */
    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus == 2 || authStatus == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }
    
    /**
     * 封装用户信息
     *
     * @param userInfo
     * @return
     */
    private UserInfo packageUserInfo(UserInfo userInfo) {
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        
        String statusString = userInfo.getStatus().intValue() == 0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }
}
