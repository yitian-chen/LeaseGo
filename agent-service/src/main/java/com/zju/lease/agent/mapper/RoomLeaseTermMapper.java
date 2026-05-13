package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.LeaseTerm;

import java.util.List;

public interface RoomLeaseTermMapper extends BaseMapper<LeaseTerm> {
    List<LeaseTerm> selectListByRoomId(Long roomId);
}
