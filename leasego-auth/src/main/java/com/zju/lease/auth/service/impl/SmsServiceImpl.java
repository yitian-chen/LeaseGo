package com.zju.lease.auth.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.zju.lease.auth.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private com.aliyun.dypnsapi20170525.Client client;

    @Override
    public String sendCode(String phone) throws Exception {
        com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest request = new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                .setSignName("速通互联验证码")
                .setTemplateCode("100001")
                .setPhoneNumber(phone)
                .setReturnVerifyCode(true)
                .setTemplateParam("{\"code\":\"##code##\",\"min\":\"5\"}");

        RuntimeOptions runtime = new RuntimeOptions();
        SendSmsVerifyCodeResponse resp = client.sendSmsVerifyCodeWithOptions(request, runtime);

        log.info("SMS response: {}", resp);
        return resp.getBody().getModel().getVerifyCode();
    }
}
