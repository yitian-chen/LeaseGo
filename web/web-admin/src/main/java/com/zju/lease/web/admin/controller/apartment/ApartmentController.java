package com.zju.lease.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zju.lease.common.result.Result;
import com.zju.lease.model.entity.ApartmentInfo;
import com.zju.lease.model.enums.ReleaseStatus;
import com.zju.lease.web.admin.service.ApartmentInfoService;
import com.zju.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.zju.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.zju.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.zju.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "公寓信息管理")
@RestController
@RequestMapping("/admin/apartment")
public class ApartmentController {

    @Autowired
    private ApartmentInfoService service;

    @Operation(summary = "保存或更新公寓信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody ApartmentSubmitVo apartmentSubmitVo) {
        service.saveOrUpdateApartment(apartmentSubmitVo);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询公寓列表")
    @GetMapping("pageItem")
    public Result<IPage<ApartmentItemVo>> pageItem(@RequestParam long current, @RequestParam long size, ApartmentQueryVo queryVo) {
        Page<ApartmentItemVo> page = new Page<>(current, size);
        // 由于需要多表查询（房间总数与空余房间数需要查房间表和租约表），因此需要自定义SQL
        IPage<ApartmentItemVo> result = service.pageItem(page, queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据id获取公寓详细信息")
    @GetMapping("getDetailById")
    public Result<ApartmentDetailVo> getDetailById(@RequestParam Long id) {
        // 由于需要来自很多表的数据，因此直接在代码中实现多表查询，而非在SQL语句中
        ApartmentDetailVo result = service.getDetailById(id);
        return Result.ok(result);
    }

    @Operation(summary = "根据id删除公寓信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        service.removeApartmentById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改公寓发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result updateReleaseStatusById(@RequestParam Long id, @RequestParam ReleaseStatus status) {
        LambdaUpdateWrapper<ApartmentInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApartmentInfo::getId, id);
        updateWrapper.set(ApartmentInfo::getIsRelease, status);
        service.update(updateWrapper);
        return Result.ok();
    }

    @Operation(summary = "根据区县id查询公寓信息列表")
    @GetMapping("listInfoByDistrictId")
    public Result<List<ApartmentInfo>> listInfoByDistrictId(@RequestParam Long id) {
        LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApartmentInfo::getDistrictId, id);
        List<ApartmentInfo> list = service.list(queryWrapper);
        return Result.ok(list);
    }
}














