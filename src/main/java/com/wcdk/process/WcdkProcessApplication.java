package com.wcdk.process;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@SpringBootApplication
@MapperScan("com.wcdk.process.mapper")
public class WcdkProcessApplication {

    public static void main(String[] args) {
        SpringApplication.run(WcdkProcessApplication.class, args);
    }
}
