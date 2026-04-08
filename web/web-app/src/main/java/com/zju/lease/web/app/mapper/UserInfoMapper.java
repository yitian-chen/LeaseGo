package com.zju.lease.web.app.mapper;

import com.zju.lease.model.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author liubo
* @description 针对表【user_info(用户信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.zju.lease.model.entity.UserInfo
*/
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    // 专门用于登录校验，会把 select=false 的 password 也查出来
    UserInfo selectOneByPhone(String phone);

    // 根据手机号精确搜索
    UserInfo selectByPhone(@Param("phone") String phone);

    // 根据昵称模糊搜索
    java.util.List<UserInfo> selectByNicknameLike(@Param("nickname") String nickname);
}




