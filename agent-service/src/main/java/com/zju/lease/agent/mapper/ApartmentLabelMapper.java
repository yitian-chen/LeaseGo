package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.LabelInfo;

import java.util.List;

public interface ApartmentLabelMapper extends BaseMapper<LabelInfo> {
    List<LabelInfo> selectListByApartmentId(Long apartmentId);
}
