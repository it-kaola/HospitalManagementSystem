package com.bjpowernode.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.bjpowernode.yygh.user.dao")
public class UserInfoConfig {

}
