package com.xmd.usermanage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xmd.usermanage.mapper")
public class UserManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManageApplication.class, args);
    }

}