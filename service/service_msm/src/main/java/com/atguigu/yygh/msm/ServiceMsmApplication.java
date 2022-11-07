package com.atguigu.yygh.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;



/**
 * @author: Wei
 * @date: 2022/10/30,16:26
 * @description:
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) // 排除自动装配
@ComponentScan("com.atguigu")
public class ServiceMsmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMsmApplication.class,args);
    }
}
