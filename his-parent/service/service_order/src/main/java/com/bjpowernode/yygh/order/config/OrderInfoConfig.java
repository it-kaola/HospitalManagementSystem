package com.bjpowernode.yygh.order.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.bjpowernode.yygh.order.dao")
public class OrderInfoConfig {
}
