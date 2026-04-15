package com.zju.lease.auth.service.impl;

import com.zju.lease.auth.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String templateCode;

    @Override
    public String sendCode(String phone) throws Exception {
        // 短信发送功能需要配置阿里云短信SDK
        // 这里生成一个模拟验证码用于测试
        // 实际使用时需要集成阿里云短信服务
        log.warn("SMS service not fully configured. Using mock code for phone: {}", phone);

        // 生成6位验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        log.info("Mock SMS code for {}: {}", phone, code);

        return code;
    }
}
