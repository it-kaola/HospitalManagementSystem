package com.bjpowernode.yygh.msm.service;

import com.bjpowernode.yygh.vo.msm.MsmVo;

public interface MSMService {

    // 发送验证码
    boolean sendCode(String phone, String code);

    // mq发送短信的接口
    boolean sendMessageByMQ(MsmVo msmVo);

}
