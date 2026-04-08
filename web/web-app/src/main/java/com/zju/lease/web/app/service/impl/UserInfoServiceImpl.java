package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.web.app.service.UserInfoService;
import com.zju.lease.web.app.mapper.UserInfoMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author liubo
* @description 针对表【user_info(用户信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService {

    @Override
    public List<UserInfo> searchUsers(String keyword, Long excludeUserId) {
        List<UserInfo> results = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return results;
        }

        keyword = keyword.trim();

        // 如果是11位纯数字，按手机号精确搜索
        if (keyword.matches("^1[3-9]\\d{9}$")) {
            UserInfo user = baseMapper.selectByPhone(keyword);
            if (user != null && !user.getId().equals(excludeUserId)) {
                results.add(user);
            }
        } else {
            // 否则按昵称模糊搜索
            List<UserInfo> users = baseMapper.selectByNicknameLike(keyword);
            for (UserInfo user : users) {
                if (!user.getId().equals(excludeUserId)) {
                    results.add(user);
                }
            }
        }

        return results;
    }
}




