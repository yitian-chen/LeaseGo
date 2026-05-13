package com.zju.lease.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.FacilityInfo;

import java.util.List;

public interface ApartmentFacilityMapper extends BaseMapper<FacilityInfo> {
    List<FacilityInfo> selectListByApartmentId(Long apartmentId);
}
