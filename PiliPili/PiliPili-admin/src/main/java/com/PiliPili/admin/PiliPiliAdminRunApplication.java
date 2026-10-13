package com.PiliPili.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"com.PiliPili"})
@MapperScan("com.PiliPili.mapper")
public class PiliPiliAdminRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(PiliPiliAdminRunApplication.class, args);
    }
}
