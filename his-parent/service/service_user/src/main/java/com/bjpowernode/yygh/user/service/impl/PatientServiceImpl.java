package com.bjpowernode.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjpowernode.yygh.cmn.client.DictFeignClient;
import com.bjpowernode.yygh.enums.DictEnum;
import com.bjpowernode.yygh.model.user.Patient;
import com.bjpowernode.yygh.user.dao.PatientDao;
import com.bjpowernode.yygh.user.service.PatientService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientDao, Patient> implements PatientService {

    @Resource
    private DictFeignClient dictFeignClient;

    // 获取就诊人列表接口
    @Override
    public List<Patient> findAllByUserId(Long userId) {

        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Patient> patientList = baseMapper.selectList(queryWrapper);
        for(Patient patient : patientList){
            // 通过远程调用，得到编号对应的具体内容

            // 证件类型的名称
            String certificatesTypeString = dictFeignClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
            // 联系人证件类型名称
            String contactsCertificatesTypeString = dictFeignClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
            // 省
            String provinceString = dictFeignClient.getDictName(patient.getProvinceCode());
            // 市
            String cityString = dictFeignClient.getDictName(patient.getCityCode());
            // 区
            String districtString = dictFeignClient.getDictName(patient.getDistrictCode());
            // 将以上的信息都封装到Patient对象中的param属性中
            patient.getParam().put("certificatesTypeString", certificatesTypeString);
            patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
            patient.getParam().put("provinceString", provinceString);
            patient.getParam().put("cityString", cityString);
            patient.getParam().put("districtString", districtString);
            patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        }
        return patientList;
    }


    // 根据id获取就诊人的信息
    @Override
    public Patient getPatientById(Long id) {

        Patient patient = baseMapper.selectById(id);

        // 证件类型的名称
        String certificatesTypeString = dictFeignClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        // 联系人证件类型名称
        String contactsCertificatesTypeString = dictFeignClient.getDictName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        // 省
        String provinceString = dictFeignClient.getDictName(patient.getProvinceCode());
        // 市
        String cityString = dictFeignClient.getDictName(patient.getCityCode());
        // 区
        String districtString = dictFeignClient.getDictName(patient.getDistrictCode());
        // 将以上的信息都封装到Patient对象中的param属性中
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());

        return patient;
    }
}
