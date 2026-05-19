package com.zju.lease.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zju.lease.common.result.Result;
import com.zju.lease.model.entity.RoomInfo;
import com.zju.lease.model.enums.ReleaseStatus;
import com.zju.lease.web.admin.service.RoomInfoService;
import com.zju.lease.web.admin.vo.room.LandlordSelectVo;
import com.zju.lease.web.admin.vo.room.RoomDetailVo;
import com.zju.lease.web.admin.vo.room.RoomItemVo;
import com.zju.lease.web.admin.vo.room.RoomQueryVo;
import com.zju.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房间信息管理")
@RestController
@RequestMapping("/admin/room")
public class RoomController {

    @Autowired
    private RoomInfoService service;

    @Operation(summary = "保存或更新房间信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody RoomSubmitVo roomSubmitVo) {
        service.saveOrUpdateRoom(roomSubmitVo);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemVo>> pageItem(@RequestParam long current, @RequestParam long size, RoomQueryVo queryVo) {
        Page<RoomItemVo> page = new Page<>(current, size);
        IPage<RoomItemVo> result = service.pageItem(page, queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据id获取房间详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailVo> getDetailById(@RequestParam Long id) {
        RoomDetailVo result = service.getDetailById(id);
        return Result.ok(result);
    }

    @Operation(summary = "根据id删除房间信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        service.removeRoomById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改房间发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result updateReleaseStatusById(Long id, ReleaseStatus status) {
        service.updateReleaseStatusById(id, status);
        return Result.ok();
    }

    @GetMapping("listBasicByApartmentId")
    @Operation(summary = "根据公寓id查询房间列表")
    public Result<List<RoomInfo>> listBasicByApartmentId(Long id) {
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomInfo::getApartmentId, id);
        List<RoomInfo> list = service.list(queryWrapper);
        return Result.ok(list);
    }

    @Operation(summary = "获取房东列表(用于选择房东)")
    @GetMapping("listLandlords")
    public Result<List<LandlordSelectVo>> listLandlords() {
        List<LandlordSelectVo> result = service.listLandlords();
        return Result.ok(result);
    }

    @Operation(summary = "根据房间id获取房东信息")
    @GetMapping("getLandlordByRoomId")
    public Result<LandlordSelectVo> getLandlordByRoomId(@RequestParam Long id) {
        LandlordSelectVo result = service.getLandlordByRoomId(id);
        return Result.ok(result);
    }

}
