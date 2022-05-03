package com.bjpowernode.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bjpowernode.yygh.model.user.UserInfo;
import com.bjpowernode.yygh.vo.user.LoginVo;
import com.bjpowernode.yygh.vo.user.UserAuthVo;
import com.bjpowernode.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    // 通过手机号和验证码进行登录验证
    Map<String, Object> loginByPhone(LoginVo loginVo);

    // 根据openid判断数据库中是否存在微信扫码人的信息
    UserInfo selectByOpenId(String openid);

    // 用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    // 用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> page, UserInfoQueryVo userInfoQueryVo);

    // 锁定用户状态
    void lockStatus(Long userId, Integer status);

    // 显示用户的详细信息
    Map<String, Object> showUserDetail(Long userId);

    // 修改用户的认证状态
    void approvalAuthStatus(Long userId, Integer authStatus);
}
