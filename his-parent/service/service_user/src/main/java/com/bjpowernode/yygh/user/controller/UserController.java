package com.bjpowernode.yygh.user.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.model.user.UserInfo;
import com.bjpowernode.yygh.user.service.UserInfoService;
import com.bjpowernode.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "管理员平台对于用户操作的相关接口")
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("用户列表（条件查询带分页）")
    @GetMapping("/{pageNo}/{limit}")
    public Result UserList(@PathVariable("pageNo") Long pageNo, @PathVariable("limit") Long limit, UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> page = new Page<UserInfo>(pageNo, limit);
        IPage<UserInfo> pageModel = userInfoService.selectPage(page, userInfoQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("锁定用户状态")
    @GetMapping("/lockStatus/{userId}/{status}")
    public Result lockStatus(@PathVariable("userId") Long userId, @PathVariable("status") Integer status){
        userInfoService.lockStatus(userId, status);
        return Result.ok();
    }

    @ApiOperation("显示用户的详细信息")
    @GetMapping("/showUserDetail/{userId}")
    public Result showUserDetail(@PathVariable("userId") Long userId){
        Map<String, Object> map = userInfoService.showUserDetail(userId);
        return Result.ok(map);
    }

    @ApiOperation("修改用户的认证状态")
    @GetMapping("/approvalAuthStatus/{userId}/{authStatus}")
    public Result approvalAuthStatus(@PathVariable("userId") Long userId, @PathVariable("authStatus") Integer authStatus){
        userInfoService.approvalAuthStatus(userId, authStatus);
        return Result.ok();
    }

}
