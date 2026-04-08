package com.zju.lease.web.app.service;

import com.zju.lease.model.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【user_info(用户信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface UserInfoService extends IService<UserInfo> {

    List<UserInfo> searchUsers(String keyword, Long excludeUserId);
}