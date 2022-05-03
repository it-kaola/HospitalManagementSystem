package com.bjpowernode.yygh.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) // 取消数据源的自动配置
@ComponentScan(basePackages = "com.bjpowernode")
@EnableDiscoveryClient
public class ServiceMSMApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMSMApplication.class, args);
    }
}
