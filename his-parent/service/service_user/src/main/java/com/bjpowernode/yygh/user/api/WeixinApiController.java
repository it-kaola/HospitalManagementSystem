package com.bjpowernode.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.bjpowernode.yygh.common.helper.JwtHelper;
import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.model.user.UserInfo;
import com.bjpowernode.yygh.user.service.UserInfoService;
import com.bjpowernode.yygh.user.utils.ConstantWxPropertiesUtil;
import com.bjpowernode.yygh.user.utils.HttpClientUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "微信操作的相关接口")
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {

    @Resource
    UserInfoService userInfoService;

    @ApiOperation("微信扫描后回调的方法")
    @GetMapping("/callback")
    public String callback(String code, String state){
        // 第一步：获取临时票据 code
        System.out.println("code：" + code);
        // 第二步：拿着code、微信id、微信秘钥，请求微信提供的固定地址，得到openid和access_token
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtil.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtil.WX_OPEN_APP_SECRET,
                code);

        try {
            String info = HttpClientUtils.get(accessTokenUrl);
            System.out.println("info：" + info);
            // 从返回的字符串中获取两个值：openid和access_token
            JSONObject jsonObject = JSONObject.parseObject(info); // 将返回的json格式字符串转为json
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");

            // 根据openid判断数据库中是否存在微信扫码人的信息
            UserInfo userInfo = userInfoService.selectByOpenId(openid);

            if(userInfo == null){
                // 第三步：根据前面获取到的access_token和openid请求微信提供的固定地址，得到扫码人的个人信息
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                String userWechatInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("userWechatInfo：" + userWechatInfo);
                JSONObject resultUserInfoJson = JSONObject.parseObject(userWechatInfo);
                // 用户的昵称
                String nickname = resultUserInfoJson.getString("nickname");
                // 用户的头像
                String headimgurl = resultUserInfoJson.getString("headimgurl");

                // 将获取到的扫码人信息封装到userInfo对象中，添加到数据库中
                userInfo = new UserInfo();
                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }

            // 将name和token的值封装到Map集合中
            Map<String, Object> map = new HashMap<>();
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name", name);


            if(StringUtils.isEmpty(userInfo.getPhone())) { // userInfo中手机号若为空，表示用户第一次使用微信登录平台，需要绑定手机号发送验证码
                map.put("openid", userInfo.getOpenid());
            } else {
                map.put("openid", ""); // userInfo中手机号若不为空，表示用户之前已经使用过微信登录平台，不需要再绑定手机号发送验证码
            }
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);

            // 重定向
            return "redirect:" + ConstantWxPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="+URLEncoder.encode((String)map.get("name"), "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    // 生成微信扫描的二维码，返回生成二维码的相关参数
    @ApiOperation("生成微信扫描的二维码，返回生成二维码的相关参数")
    @GetMapping("/getLoginParam")
    @ResponseBody
    public Result getLoginParam(){
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("appid", ConstantWxPropertiesUtil.WX_OPEN_APP_ID);
            map.put("scope", "snsapi_login");
            String redirectUri = ConstantWxPropertiesUtil.WX_OPEN_REDIRECT_URL;
            URLEncoder.encode(redirectUri,"utf-8");
            map.put("redirect_uri", redirectUri);
            map.put("state", System.currentTimeMillis()+"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        return Result.ok(map);
    }


    // 回调的方法，得到扫描人的信息

}
