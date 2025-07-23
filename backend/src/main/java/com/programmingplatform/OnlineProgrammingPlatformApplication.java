package com.programmingplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 在线编程学习平台主应用类
 * 
 * @author Programming Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@MapperScan("com.programmingplatform.mapper")
public class OnlineProgrammingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineProgrammingPlatformApplication.class, args);
    }
}
