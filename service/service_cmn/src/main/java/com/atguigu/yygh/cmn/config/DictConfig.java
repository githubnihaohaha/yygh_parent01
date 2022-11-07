package com.atguigu.yygh.cmn.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Wei
 * @date: 2022/10/21,14:01
 * @description:
 */
@Configuration
@MapperScan("com.atguigu.yygh.cmn.mapper")
public class DictConfig {
}
