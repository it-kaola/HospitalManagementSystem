package com.bjpowernode.yygh.hosp.service;

import com.bjpowernode.yygh.model.hosp.Hospital;
import com.bjpowernode.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {

    // 上传医院信息接口
    void save(Map<String, Object> paramMap);

    // 根据hoscode查询医院信息
    Hospital getHospitalByHoscode(String hoscode);

    // 查询医院列表（条件查询+分页）
    Page<Hospital> selectHospPage(Integer pageNo, Integer limit, HospitalQueryVo hospitalQueryVo);

    // 更新医院上线状态
    void updateHospStatus(String id, Integer status);

    // 获取医院详细信息
    Map<String, Object> getHospitalDetail(String id);

    // 获取医院名称
    String gethospitalNameByHoscode(String hoscode);

    // 根据医院名称查询医院（包括模糊查询）
    List<Hospital> getHospitalByHosname(String hosname);

    // 根据医院编号获取预约挂号的详细信息
    Map<String, Object> getHospAppointmentDetail(String hoscode);
}
