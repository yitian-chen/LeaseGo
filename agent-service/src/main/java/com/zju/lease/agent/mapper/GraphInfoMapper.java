package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.GraphInfo;

import java.util.List;

public interface GraphInfoMapper extends BaseMapper<GraphInfo> {
    List<GraphInfo> selectListByRoomId(Long roomId);
}
