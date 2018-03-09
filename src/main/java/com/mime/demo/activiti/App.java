package com.mime.demo.activiti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author SlumDuck
 * @create 2018-03-09 10:26
 * @desc
 */
@SpringBootApplication
@ComponentScan
@ServletComponentScan
@EnableTransactionManagement
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
