package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.AttrValue;

import java.util.List;

public interface RoomAttrValueMapper extends BaseMapper<AttrValue> {
    List<AttrValue> selectListByRoomId(Long roomId);
}
