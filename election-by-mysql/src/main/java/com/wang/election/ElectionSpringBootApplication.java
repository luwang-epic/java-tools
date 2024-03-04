package com.wang.election;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// 指定Mapper接口所在的包路径
@MapperScan("com.wang.election.mapper")
// 需要开启调度任务，用于选主续期
@EnableScheduling
public class ElectionSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectionSpringBootApplication.class, args);
    }

}