package com.bjpowernode.yygh.msm.entity;

import lombok.Data;

@Data
public class Sms {
    // 手机号
    private String phoneNum;
    // 短信验证码
    private String code;
    // 有效时间
    private Integer min;
}
