package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.PaymentType;

import java.util.List;

public interface RoomPaymentTypeMapper extends BaseMapper<PaymentType> {
    List<PaymentType> selectListByRoomId(Long roomId);
}
