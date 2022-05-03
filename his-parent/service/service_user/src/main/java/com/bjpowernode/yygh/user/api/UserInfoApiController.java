package com.bjpowernode.yygh.user.api;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.common.utils.AuthContextHolder;
import com.bjpowernode.yygh.model.user.UserInfo;
import com.bjpowernode.yygh.user.service.UserInfoService;
import com.bjpowernode.yygh.vo.user.LoginVo;
import com.bjpowernode.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("通过手机号和验证码进行登录验证")
    @PostMapping("/loginByPhone")
    public Result loginByPhone(@RequestBody LoginVo loginVo){
        Map<String, Object> info =  userInfoService.loginByPhone(loginVo);
        return Result.ok(info);
    }


    // 用户认证的接口
    @ApiOperation("用户认证的接口")
    @PostMapping("/auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        // 传递两个参数，一个是用户的id，另一个是UserAuthVo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request), userAuthVo);
        return Result.ok();
    }


    // 获取完整用户信息
    @ApiOperation("获取完整用户信息")
    @GetMapping("/auth/getUserInfoById")
    public Result getUserInfoById(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }

}
