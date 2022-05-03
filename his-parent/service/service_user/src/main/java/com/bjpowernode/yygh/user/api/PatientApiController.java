package com.bjpowernode.yygh.user.api;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.common.utils.AuthContextHolder;
import com.bjpowernode.yygh.model.user.Patient;
import com.bjpowernode.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "就诊人相关接口")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Resource
    PatientService patientService;


    @ApiOperation("获取就诊人列表接口")
    @GetMapping("/auth/findAll")
    public Result findAll(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> patientList = patientService.findAllByUserId(userId);
        return Result.ok(patientList);
    }


    @ApiOperation("添加就诊人接口")
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }


    @ApiOperation("根据id获取就诊人的信息")
    @GetMapping("auth/get/{id}")
    public Result getPatientById(@PathVariable("id") Long id){
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }


    @ApiOperation("修改就诊人信息")
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }


    @ApiOperation("删除就诊人信息")
    @DeleteMapping("auth/remove/{id}")
    public Result removePatientById(@PathVariable("id") Long id){
        patientService.removeById(id);
        return Result.ok();
    }

    @ApiOperation("由其他服务调用的接口，根据id查询就诊人的信息")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id){
        return patientService.getPatientById(id);
    }
}
