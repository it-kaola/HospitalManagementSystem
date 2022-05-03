package com.bjpowernode.yygh.common.utils;

/*
* 根据token获取用户信息的工具类
* */

import com.bjpowernode.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

public class AuthContextHolder {

    // 获取当前用户id
    public static Long getUserId(HttpServletRequest request){
        // 通过header获取token
        String token = request.getHeader("token");
        // 通过JWT解析token，获取用户Id
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }


    // 获取当前用户姓名
    public static String getUserName(HttpServletRequest request){
        // 通过header获取token
        String token = request.getHeader("token");
        // 通过JWT解析token，获取用户Id
        String userName = JwtHelper.getUserName(token);
        return userName;
    }


}
