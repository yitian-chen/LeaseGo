package com.zju.lease.auth.service;

public interface SmsService {
    String sendCode(String phone) throws Exception;
}
