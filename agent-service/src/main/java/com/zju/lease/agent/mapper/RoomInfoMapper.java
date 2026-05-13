package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.RoomInfo;

import java.util.List;

public interface RoomInfoMapper extends BaseMapper<RoomInfo> {
    List<RoomInfo> selectAllReleasedRooms();
}
