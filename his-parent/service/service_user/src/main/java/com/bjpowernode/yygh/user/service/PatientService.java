package com.bjpowernode.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bjpowernode.yygh.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {

    // 获取就诊人列表接口
    List<Patient> findAllByUserId(Long userId);

    // 根据id获取就诊人的信息
    Patient getPatientById(Long id);
}
