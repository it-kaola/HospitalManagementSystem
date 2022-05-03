package com.bjpowernode.yygh.hosp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bjpowernode.yygh.model.hosp.HospitalSet;
import com.bjpowernode.yygh.vo.order.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {

    String getSignKeyByHoscode(String hoscode);

    // 获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);
}
