package com.zju.lease.upms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.SystemUser;
import org.apache.ibatis.annotations.Param;

public interface SystemUserMapper extends BaseMapper<SystemUser> {

    SystemUser selectOneByUsername(@Param("username") String username);
}
