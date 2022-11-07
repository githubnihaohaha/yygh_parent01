package com.atguigu.yygh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author: Wei
 * @date: 2022/10/26,22:49
 * @description:
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(ApiGatewayApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
