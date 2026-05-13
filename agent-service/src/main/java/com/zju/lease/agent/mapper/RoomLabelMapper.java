package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.LabelInfo;

import java.util.List;

public interface RoomLabelMapper extends BaseMapper<LabelInfo> {
    List<LabelInfo> selectListByRoomId(Long roomId);
}
