package com.atguigu.yygh.order.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Wei
 * @date: 2022/11/4,20:23
 * @description:
 */
@Configuration
@MapperScan("com.atguigu.yygh.order.mapper")
public class ServiceOrderConfig {
}
