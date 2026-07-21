package com.fons.cloud.auth;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 账号授权服务 提供auth2授权、用户相关服务等
 * @author qiyuan.hong
 * @date 2022-03-10 21:43
 */
@EnableDubbo
@SpringBootApplication
@EnableDiscoveryClient
public class AccountAuthMain {

    public static void main(String[] args) {
        SpringApplication.run(AccountAuthMain.class, args);
    }


}
