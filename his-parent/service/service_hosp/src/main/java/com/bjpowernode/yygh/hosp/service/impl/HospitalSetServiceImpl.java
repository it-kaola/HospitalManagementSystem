package com.bjpowernode.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjpowernode.yygh.common.exception.YyghException;
import com.bjpowernode.yygh.common.result.ResultCodeEnum;
import com.bjpowernode.yygh.hosp.dao.HospitalSetDao;
import com.bjpowernode.yygh.hosp.service.HospitalSetService;
import com.bjpowernode.yygh.model.hosp.HospitalSet;
import com.bjpowernode.yygh.vo.order.SignInfoVo;
import org.springframework.stereotype.Service;

/*
* 说明：
*   com.baomidou.mybatisplus.extension.service.impl.ServiceImpl类已经默认实现了单表的CRUD，分页查询也有默认实现，能够更加灵活和代码简洁把分页查询功能实现。
* */

@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetDao, HospitalSet> implements HospitalSetService {

    // 在这个类中需要使用到HospitalSetDao对象操作数据库，原本是需要依赖注入的，但是ServiceImpl这个类里面，已将帮我们将HospitalSetDao对象注入好了，所以不需要我们手动写出来了
    /*@Resource
    private HospitalSetDao hospitalSetDao;*/

    // 根据医院编号获取医院签名
    @Override
    public String getSignKeyByHoscode(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }

    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(null == hospitalSet) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;

    }

}
