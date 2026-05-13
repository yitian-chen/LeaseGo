package com.zju.lease.agent;

import com.zju.lease.common.minio.MinioConfiguration;
import com.zju.lease.common.sms.AliyunSMSConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.zju.lease.agent.mapper")
@EnableAsync
@EnableScheduling
@Import(com.zju.lease.common.redis.RedisConfiguration.class)
@ComponentScan(basePackages = {
        "com.zju.lease.agent",
        "com.zju.lease.common.redis",
        "com.zju.lease.common.interceptor",
        "com.zju.lease.common.mybatisplus"
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {MinioConfiguration.class, AliyunSMSConfiguration.class}))
public class AgentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentServiceApplication.class, args);
    }
}
