package com.bjpowernode.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjpowernode.yygh.common.exception.YyghException;
import com.bjpowernode.yygh.common.helper.JwtHelper;
import com.bjpowernode.yygh.common.result.ResultCodeEnum;
import com.bjpowernode.yygh.enums.AuthStatusEnum;
import com.bjpowernode.yygh.model.user.Patient;
import com.bjpowernode.yygh.model.user.UserInfo;
import com.bjpowernode.yygh.user.dao.UserInfoDao;
import com.bjpowernode.yygh.user.service.PatientService;
import com.bjpowernode.yygh.user.service.UserInfoService;
import com.bjpowernode.yygh.vo.user.LoginVo;
import com.bjpowernode.yygh.vo.user.UserAuthVo;
import com.bjpowernode.yygh.vo.user.UserInfoQueryVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoDao, UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private PatientService patientService;


    // 通过手机号和验证码进行登录验证
    @Override
    public Map<String, Object> loginByPhone(LoginVo loginVo) {

        // 从loginVo中获取用户输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        // 判断手机号和验证码是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 判断手机收到的验证码和用户输入的验证码是否一致
        String redisCode = redisTemplate.opsForValue().get(phone);
        if(! code.equals(redisCode)){
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //绑定手机号码
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(loginVo.getOpenid())) { // 如果是使用微信第一次登录的话，手机号一定为空，openid一定不为空
            userInfo = this.selectByOpenId(loginVo.getOpenid());
            if(null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }

        if(userInfo == null){
            // 判断用户是否为第一次登录，根据手机号去数据库中查询，如果有相应记录则表示是已经注册过的用户，反之则为第一次登录的用户
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(queryWrapper);
            if(userInfo == null){
                // 程序执行到此处，证明用户是第一次登录，先进行注册，将信息添加到数据库中
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                // 将用户信息添加到数据库中
                baseMapper.insert(userInfo);
            }
        }


        //校验是否被禁用
        if(userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        // 返回登录的相关信息，例如用户的名称，token等
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        // 生成token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);

        return map;
    }


    // 根据openid判断数据库中是否存在微信扫码人的信息
    @Override
    public UserInfo selectByOpenId(String openid) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        return baseMapper.selectOne(queryWrapper);
    }


    // 用户认证
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        // 根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);

        // 设置用户信息，完善用户信息
        // 当前用户的真实姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        userInfo.setUpdateTime(new Date());
        // 更新用户信息
        baseMapper.updateById(userInfo);
    }


    // 用户列表（条件查询带分页）
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> page, UserInfoQueryVo userInfoQueryVo) {

        // 通过UserInfoQueryVo对象获取条件值
        String name = userInfoQueryVo.getKeyword(); // 用户的真实姓名
        Integer status = userInfoQueryVo.getStatus(); // 用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); // 认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); // 创建用户记录的起始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); // 创建用户记录的结束时间
        // 对条件值进行非空判断，如果非空则
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        if(! StringUtils.isEmpty(name)){
            queryWrapper.like("name", name);
        }
        if(! StringUtils.isEmpty(status)){
            queryWrapper.eq("status", status);
        }
        if(! StringUtils.isEmpty(authStatus)){
            queryWrapper.eq("auth_status", authStatus);
        }
        if(! StringUtils.isEmpty(createTimeBegin)){
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if(! StringUtils.isEmpty(createTimeEnd)){
            queryWrapper.le("create_time", createTimeEnd);
        }
        // 调用baseMapper中的方法，实现分页功能
        Page<UserInfo> userInfoPage = baseMapper.selectPage(page, queryWrapper);

        List<UserInfo> UserInfoList = userInfoPage.getRecords();

        for(UserInfo userInfo : UserInfoList){
            // 将查询到的userInfo对象中对应的编号转为对应的中文含义，放入userInfo对象param参数

            // 认证状态的编号
            userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
            // 用户状态的编号
            String statusString = userInfo.getStatus() == 1 ? "正常" : "锁定";
            userInfo.getParam().put("statusString", statusString);
        }

        return userInfoPage;
    }


    // 锁定用户状态
    @Override
    public void lockStatus(Long userId, Integer status) {
        if(status == 0 || status == 1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }


    // 显示用户的详细信息
    @Override
    public Map<String, Object> showUserDetail(Long userId) {

        Map<String, Object> map = new HashMap<>();

        // 根据userId查询出对应的userInfo对象
        UserInfo userInfo = baseMapper.selectById(userId);

        // 将userInfo对象中的状态编号设置为对应的中文

        // 认证状态的编号
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        // 用户状态的编号
        String statusString = userInfo.getStatus() == 1 ? "正常" : "锁定";
        userInfo.getParam().put("statusString", statusString);

        // 根据userId查询出该用户所绑定的就诊人
        List<Patient> patientList = patientService.findAllByUserId(userId);

        map.put("userInfo", userInfo);
        map.put("patientList", patientList);

        return map;
    }


    // 修改用户的认证状态
    @Override
    public void approvalAuthStatus(Long userId, Integer authStatus) {
        if(authStatus == -1 || authStatus == 2){ // -1表示不通过，2表示通过
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }

    }


}

