package com.mime.demo.activiti.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;

/**
 * @author SlumDuck
 * @create 2018-03-08 14:19
 * @desc 公共配置类加载
 */
@Configuration
public class CommonConfiguration {

    /**
     * 配置数据源
     * @return
     */
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean("dataSource")
    public DataSource dataSource(){
        DataSource dataSource = new DruidDataSource();
        return dataSource;
    }

    /**
     * Jackson，json解析对象
     * @return
     */
    @Bean("objectMapper")
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    /**
     * activiti内部使用的UUID生成策略
     * @return
     */
    @Bean("uuidGenerator")
    public IdGenerator idGenerator(){
        return new StrongUuidGenerator();
    }

}
