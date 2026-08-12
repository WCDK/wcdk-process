package com.wcdk.process;

import com.wcdk.r2dbc.config.EnableWcdkR2dbcRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableWcdkR2dbcRepositories(basePackages = {"com.wcdk.process.repository"})
/**
 * WCDK 濞翠胶鈻煎Ο鈥虫健缁鐎烽妴?
 *
 * @author WCDK
 * @version 1.0
 */
public class WcdkProcessApplication {

    public static void main(String[] args) {
        SpringApplication.run(WcdkProcessApplication.class, args);
    }
}