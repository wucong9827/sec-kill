package com.example.seckillservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.seckilldao.mapper")
public class SeckillServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillServiceApplication.class, args);
    }

}
