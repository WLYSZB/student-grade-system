package com.example.gradesystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.gradesystem.mapper")
public class GradeSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradeSystemApplication.class, args);
    }
}
