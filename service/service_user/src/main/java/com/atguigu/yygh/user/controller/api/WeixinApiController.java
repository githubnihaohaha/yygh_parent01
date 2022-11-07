package com.atguigu.yygh.user.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantProperties;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Wei
 * @date: 2022/10/31,18:17
 * @description:
 */
@Controller
@RequestMapping("/api/user/wx")
public class WeixinApiController {
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 将加载微信二维码所需的信息返回给前端
     *
     * @return 一个map集合, 包含 appid,redirect_uri,scope
     */
    @GetMapping("/getLoginParam")
    @ResponseBody
    public R getLoginParams() {
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantProperties.WX_OPEN_APP_ID);
        map.put("scope", "snsapi_login");
        
        String wxOpenRedirectUrl = ConstantProperties.WX_OPEN_REDIRECT_URL;
        
        try {
            wxOpenRedirectUrl = URLEncoder.encode(wxOpenRedirectUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        map.put("redirectUri", wxOpenRedirectUrl);
        
        map.put("state", System.currentTimeMillis() + "");
        return R.ok().data(map);
    }
    
    /**
     * 用户扫码操作后的回调方法
     *
     * @param code    授权的临时票据
     * @param state
     * @param session
     * @return 页面跳转
     */
    @GetMapping("/callback")
    public String callback(String code, String state, HttpSession session) {
        
        /*
         * 携带appid,app_secret&code 请求微信的固定接口,
         * 得到openid和access_token
         *
         * */
        StringBuffer stringBuffer = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String resultInfo = String.format(stringBuffer.toString(),
                ConstantProperties.WX_OPEN_APP_ID,
                ConstantProperties.WX_OPEN_APP_SECRET,
                code);
        
        try {
            
            //使用 HttpClient 调用微信 API,获得结果,使用 fastJson 转换,获得 openid&access_token
            String firstResult = HttpClientUtils.get(resultInfo);
            JSONObject jsonObject = JSONObject.parseObject(firstResult);
            String openid = (String) jsonObject.get("openid");
            String access_token = (String) jsonObject.get("access_token");
            
            //通过openid在数据库中查询是否为第一次登录
            UserInfo userInfo = userInfoService.getUserInfoByOpenId(openid);
            
            //用户为第一次登录
            if (userInfo == null) {
                //拿着openid和access_token调用微信api,返回用户信息,将用户信息添加到数据表中
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                
                //获得用户信息,转换后获取昵称 nickname
                String userInfoResult = HttpClientUtils.get(userInfoUrl);
                JSONObject userInfoObject = JSONObject.parseObject(userInfoResult);
                String nickname = (String) userInfoObject.get("nickname");
                
                
                //向数据库中添加用户
                userInfo = new UserInfo();
                userInfo.setOpenid(openid);
                userInfo.setNickName(nickname);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }
            
            //返回数据
            Map<String, String> map = new HashMap<>();
            
            String name = userInfo.getName();
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if (StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name", name);
            
            /*
             * 只要用户使用微信登录后没有绑定过手机号,都需要绑定
             * 自定义逻辑,如果给前端返回了openid,则标识需要用户绑定手机号
             *
             * */
            String phone = userInfo.getPhone();
            if (StringUtils.isEmpty(phone)) {
                map.put("openid", openid);
            } else {
                map.put("openid", "");
            }
            
            //返回token
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);
            
            //跳转页面,如果需要绑定手机号跳转到绑定手机号页面,如果不需要关闭登录窗口刷新页面
            return "redirect:http://localhost:3000/weixin/callback" +
                    "?token=" + map.get("token") +
                    "&openid=" + map.get("openid") +
                    "&name=" + URLEncoder.encode(map.get("name"), "utf-8");
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
