package com.bjpowernode.yygh.user.client;

import com.bjpowernode.yygh.model.user.Patient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-user")
public interface PatientFeignClient {

    @ApiOperation("由其他服务调用的接口，根据id查询就诊人的信息")
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatientOrder(@PathVariable("id") Long id);
}
