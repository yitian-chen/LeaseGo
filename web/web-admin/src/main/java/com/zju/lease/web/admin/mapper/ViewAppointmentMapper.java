package com.zju.lease.web.admin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zju.lease.model.entity.ViewAppointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zju.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.zju.lease.web.admin.vo.appointment.AppointmentVo;

/**
* @author liubo
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.zju.lease.model.ViewAppointment
*/
public interface ViewAppointmentMapper extends BaseMapper<ViewAppointment> {

    IPage<AppointmentVo> pageAppointment(Page<AppointmentVo> page, AppointmentQueryVo queryVo);
}




