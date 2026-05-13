package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.FacilityInfo;

import java.util.List;

public interface RoomFacilityMapper extends BaseMapper<FacilityInfo> {
    List<FacilityInfo> selectListByRoomId(Long roomId);
}
