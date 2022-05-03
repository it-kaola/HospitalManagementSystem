package com.bjpowernode.yygh.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients(basePackages = "com.bjpowernode")
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan("com.bjpowernode") // 扫描com.bjpowernode包及其子包下的所有对象（包括该项目引进的依赖对应的com.bjpowernode包及其子包）
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class, args);
    }
}

