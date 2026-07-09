package com.fons.cloud.admin;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 框架控制面服务启动类，提供 admin REST API、治理编排和 auth-service Dubbo RPC 调用能力。
 */
@EnableDubbo
@SpringBootApplication
@EnableDiscoveryClient
public class AdminMain {

    public static void main(String[] args) {
        SpringApplication.run(AdminMain.class, args);
    }
}
