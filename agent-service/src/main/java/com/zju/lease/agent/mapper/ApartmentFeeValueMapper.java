package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.FeeValue;

import java.util.List;

public interface ApartmentFeeValueMapper extends BaseMapper<FeeValue> {
    List<FeeValue> selectListByApartmentId(Long apartmentId);
}
