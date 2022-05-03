package com.bjpowernode.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bjpowernode.yygh.cmn.client.DictFeignClient;
import com.bjpowernode.yygh.hosp.repository.HospitalRepository;
import com.bjpowernode.yygh.hosp.service.HospitalService;
import com.bjpowernode.yygh.model.hosp.Hospital;
import com.bjpowernode.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Resource
    private HospitalRepository hospitalRepository;

    @Resource
    private DictFeignClient dictFeignClient;


    // 上传医院信息接口
    @Override
    public void save(Map<String, Object> paramMap) {
        // 先将paramMap集合转换成一个Hospital对象，方便操作
        String mapStr = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mapStr, Hospital.class);

        // 判断MongoDB中是否存在数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);

        if(hospitalExist != null){
            // 如果数据库中存在该记录，则执行修改操作
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else{
            // 如果数据库中不存在该记录，则执行添加操作
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }

    }


    // 根据hoscode查询医院信息
    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }


    // 查询医院列表（条件查询+分页）
    @Override
    public Page<Hospital> selectHospPage(Integer pageNo, Integer limit, HospitalQueryVo hospitalQueryVo) {

        // 创建Pageable对象
        Pageable pageable = PageRequest.of(pageNo-1, limit);
        // 创建条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        // 将HospitalSetQueryVo对象转换成Hospital对象
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        // 创建Example对象
        Example<Hospital> example = Example.of(hospital, matcher);

        // 调用findAll()方法实现条件+分页查询
        Page<Hospital> page = hospitalRepository.findAll(example, pageable);

        // 从Page对象中获取每页展示的记录列表
        List<Hospital> hospitalList = page.getContent();
        for(Hospital hosp : hospitalList){
            // 为每一个Hospital对象添加医院等级属性值
            // 根据dictCode和value获取医院等级的名称
            String hostypeName = dictFeignClient.getDictName("Hostype", hosp.getHostype());// 服务的远程调用
            hosp.getParam().put("hostypeName", hostypeName);

            // 根据省市区的code，调用远程服务查询对应的省市区名称
            String provinceName = dictFeignClient.getDictName(hosp.getProvinceCode());
            String cityName = dictFeignClient.getDictName(hosp.getCityCode());
            String districtName = dictFeignClient.getDictName(hosp.getDistrictCode());
            hosp.getParam().put("fullAddress", provinceName+cityName+districtName);
        }

        return page;
    }


    // 更新医院上线状态
    @Override
    public void updateHospStatus(String id, Integer status) {
        // 根据id查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();
        // 设置医院上线状态
        hospital.setStatus(status);
        // 修改更新时间
        hospital.setUpdateTime(new Date());
        // 更新数据
        hospitalRepository.save(hospital);
    }


    // 获取医院详细信息
    @Override
    public Map<String, Object> getHospitalDetail(String id) {

        Map<String, Object> result = new HashMap<>();

        Hospital hospital = hospitalRepository.findById(id).get();
        String hostypeName = dictFeignClient.getDictName("Hostype", hospital.getHostype());
        hospital.getParam().put("hostypeName", hostypeName);
        // 根据省市区的code，调用远程服务查询对应的省市区名称
        String provinceName = dictFeignClient.getDictName(hospital.getProvinceCode());
        String cityName = dictFeignClient.getDictName(hospital.getCityCode());
        String districtName = dictFeignClient.getDictName(hospital.getDistrictCode());
        hospital.getParam().put("fullAddress", provinceName+cityName+districtName);

        result.put("hospital", hospital);

        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);

        return result;
    }


    // 获取医院名称
    @Override
    public String gethospitalNameByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital != null){
            return hospital.getHosname();
        }
        return null;
    }


    // 根据医院名称查询医院（包括模糊查询）
    @Override
    public List<Hospital> getHospitalByHosname(String hosname) {
        List<Hospital> hospitalList = hospitalRepository.getHospitalByHosnameLike(hosname);
        return hospitalList;
    }


    @Override
    public Map<String, Object> getHospAppointmentDetail(String hoscode) {
        //医院详情
        return this.getHospitalDetail(this.getHospitalByHoscode(hoscode).getId());
    }


}
