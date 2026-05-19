package com.zju.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zju.lease.common.constant.RedisConstant;
import com.zju.lease.common.rabbit.RoomMessage;
import com.zju.lease.common.rabbit.RabbitMQConfig;
import com.zju.lease.model.entity.*;
import com.zju.lease.model.enums.BaseStatus;
import com.zju.lease.model.enums.ItemType;
import com.zju.lease.model.enums.ReleaseStatus;
import com.zju.lease.web.admin.mapper.*;
import com.zju.lease.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.web.admin.vo.attr.AttrValueVo;
import com.zju.lease.web.admin.vo.graph.GraphVo;
import com.zju.lease.web.admin.vo.room.LandlordSelectVo;
import com.zju.lease.web.admin.vo.room.RoomDetailVo;
import com.zju.lease.web.admin.vo.room.RoomItemVo;
import com.zju.lease.web.admin.vo.room.RoomQueryVo;
import com.zju.lease.web.admin.vo.room.RoomSubmitVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private AttrValueMapper attrValueMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("myRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    @Transactional
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean isUpdating = roomSubmitVo.getId() != null;
        Long roomId = roomSubmitVo.getId();

        // 更新操作需要分布式锁防止并发
        RLock lock = null;
        if (isUpdating) {
            lock = acquireLock(roomId);
        }

        try {
            // 第一次缓存删除（在 DB 写之前）
            if (isUpdating) {
                String key = RedisConstant.APP_ROOM_PREFIX + roomId;
                redisTemplate.delete(key);
            }

            // 核心 DB 写入
            super.saveOrUpdate(roomSubmitVo);

            // 如果是更新，先删除所有旧关联数据
            if (isUpdating) {
                deleteOldAssociations(roomId);
            }

            // 插入新关联数据
            insertGraphList(roomSubmitVo);
            insertFacilityList(roomSubmitVo);
            insertLabelList(roomSubmitVo);
            insertAttrValueList(roomSubmitVo);
            insertPaymentTypeList(roomSubmitVo);
            insertLeaseTermList(roomSubmitVo);

            // 注册事务提交后回调：延时双删 + 通知 agent 重索引
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    Long committedRoomId = roomSubmitVo.getId();
                    if (committedRoomId != null) {
                        // 延时第二次缓存删除（TTL+DLX，1 秒后执行）
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.ROOM_EXCHANGE,
                                RabbitMQConfig.CACHE_DELETE_DELAY_KEY,
                                committedRoomId);
                        // 通知 agent-service 重新索引
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.ROOM_EXCHANGE,
                                RabbitMQConfig.ROOM_REINDEX_KEY,
                                new RoomMessage(committedRoomId, "UPDATE"));
                    }
                }
            });
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(page, queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = roomInfoMapper.selectById(id);

        // 查询所属公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

        // 查询图片列表
        List<GraphVo> graphVoList = graphInfoMapper.selectByItemTypeAndId(ItemType.ROOM, id);

        // 查询属性列表
        List<AttrValueVo> attrValueVoList = attrValueMapper.selectByRoomId(id);

        // 查询配套列表
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

        // 查询标签列表
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        // 查询支付方式列表
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        // 查询租期列表
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

        // 组装结果
        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVoList);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);

        // 设置房东信息
        if (roomInfo.getLandlordId() != null) {
            UserInfo landlordInfo = userInfoMapper.selectById(roomInfo.getLandlordId());
            roomDetailVo.setLandlordInfo(landlordInfo);
        }

        return roomDetailVo;
    }

    @Override
    @Transactional
    public void removeRoomById(Long id) {
        RLock lock = acquireLock(id);

        try {
            // 第一次缓存删除
            String key = RedisConstant.APP_ROOM_PREFIX + id;
            redisTemplate.delete(key);

            // 删除房间主记录
            super.removeById(id);

            // 删除所有关联数据
            deleteOldAssociations(id);

            // 注册事务提交后回调
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 延时第二次缓存删除
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.ROOM_EXCHANGE,
                            RabbitMQConfig.CACHE_DELETE_DELAY_KEY,
                            id);
                    // 通知 agent-service 移除索引
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.ROOM_EXCHANGE,
                            RabbitMQConfig.ROOM_REINDEX_KEY,
                            new RoomMessage(id, "DELETE"));
                }
            });
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public void updateReleaseStatusById(Long id, ReleaseStatus status) {
        RLock lock = acquireLock(id);

        try {
            // 第一次缓存删除
            String key = RedisConstant.APP_ROOM_PREFIX + id;
            redisTemplate.delete(key);

            // 更新发布状态
            LambdaUpdateWrapper<RoomInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(RoomInfo::getId, id);
            updateWrapper.set(RoomInfo::getIsRelease, status);
            super.update(updateWrapper);

            // 注册事务提交后回调
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 延时第二次缓存删除
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.ROOM_EXCHANGE,
                            RabbitMQConfig.CACHE_DELETE_DELAY_KEY,
                            id);
                    // 通知 agent-service 重新索引（发布状态变更影响搜索可见性）
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.ROOM_EXCHANGE,
                            RabbitMQConfig.ROOM_REINDEX_KEY,
                            new RoomMessage(id, "UPDATE"));
                }
            });
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<LandlordSelectVo> listLandlords() {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getStatus, BaseStatus.ENABLE);
        List<UserInfo> users = userInfoService.list(queryWrapper);

        List<LandlordSelectVo> result = new ArrayList<>();
        for (UserInfo user : users) {
            LandlordSelectVo vo = new LandlordSelectVo();
            vo.setId(user.getId());
            vo.setPhone(user.getPhone());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            result.add(vo);
        }
        return result;
    }

    @Override
    public LandlordSelectVo getLandlordByRoomId(Long roomId) {
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null || roomInfo.getLandlordId() == null) {
            return null;
        }
        UserInfo landlord = userInfoMapper.selectById(roomInfo.getLandlordId());
        if (landlord == null) {
            return null;
        }
        LandlordSelectVo vo = new LandlordSelectVo();
        vo.setId(landlord.getId());
        vo.setPhone(landlord.getPhone());
        vo.setNickname(landlord.getNickname());
        vo.setAvatarUrl(landlord.getAvatarUrl());
        return vo;
    }

    // ==================== 私有辅助方法 ====================

    private RLock acquireLock(Long roomId) {
        String lockKey = "lock:room:update:" + roomId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to acquire lock for room update: " + roomId);
            }
            return lock;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring lock for room update: " + roomId);
        }
    }

    private void deleteOldAssociations(Long roomId) {
        // 删除图片列表
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphQueryWrapper.eq(GraphInfo::getItemId, roomId);
        graphInfoService.remove(graphQueryWrapper);

        // 删除配套列表
        LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(RoomFacility::getRoomId, roomId);
        roomFacilityService.remove(facilityQueryWrapper);

        // 删除标签列表
        LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(RoomLabel::getRoomId, roomId);
        roomLabelService.remove(labelQueryWrapper);

        // 删除支付方式列表
        LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
        paymentQueryWrapper.eq(RoomPaymentType::getRoomId, roomId);
        roomPaymentTypeService.remove(paymentQueryWrapper);

        // 删除属性值关系列表
        LambdaQueryWrapper<RoomAttrValue> attrValueQueryWrapper = new LambdaQueryWrapper<>();
        attrValueQueryWrapper.eq(RoomAttrValue::getRoomId, roomId);
        roomAttrValueService.remove(attrValueQueryWrapper);

        // 删除可选租期列表
        LambdaQueryWrapper<RoomLeaseTerm> leaseTermQueryWrapper = new LambdaQueryWrapper<>();
        leaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomId);
        roomLeaseTermService.remove(leaseTermQueryWrapper);
    }

    private void insertGraphList(RoomSubmitVo roomSubmitVo) {
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)) {
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
    }

    private void insertFacilityList(RoomSubmitVo roomSubmitVo) {
        List<Long> facilityInfoList = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoList)) {
            ArrayList<RoomFacility> facilityList = new ArrayList<>();
            for (Long facilityId : facilityInfoList) {
                RoomFacility roomFacility = new RoomFacility();
                roomFacility.setFacilityId(facilityId);
                roomFacility.setRoomId(roomSubmitVo.getId());
                facilityList.add(roomFacility);
            }
            roomFacilityService.saveBatch(facilityList);
        }
    }

    private void insertLabelList(RoomSubmitVo roomSubmitVo) {
        List<Long> labelInfoList = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoList)) {
            List<RoomLabel> roomLabelList = new ArrayList<>();
            for (Long labelId : labelInfoList) {
                RoomLabel roomLabel = new RoomLabel();
                roomLabel.setRoomId(roomSubmitVo.getId());
                roomLabel.setLabelId(labelId);
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }
    }

    private void insertAttrValueList(RoomSubmitVo roomSubmitVo) {
        List<Long> attrValueVoList = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueVoList)) {
            ArrayList<RoomAttrValue> roomAttrValueList = new ArrayList<>();
            for (Long attrValueId : attrValueVoList) {
                RoomAttrValue roomAttrValue = new RoomAttrValue();
                roomAttrValue.setRoomId(roomSubmitVo.getId());
                roomAttrValue.setAttrValueId(attrValueId);
                roomAttrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueList);
        }
    }

    private void insertPaymentTypeList(RoomSubmitVo roomSubmitVo) {
        List<Long> paymentTypeList = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeList)) {
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeList) {
                RoomPaymentType roomPaymentType = new RoomPaymentType();
                roomPaymentType.setRoomId(roomSubmitVo.getId());
                roomPaymentType.setPaymentTypeId(paymentTypeId);
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }
    }

    private void insertLeaseTermList(RoomSubmitVo roomSubmitVo) {
        List<Long> leaseTermList = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermList)) {
            ArrayList<RoomLeaseTerm> roomLeaseTermList = new ArrayList<>();
            for (Long leaseTermId : leaseTermList) {
                RoomLeaseTerm roomLeaseTerm = new RoomLeaseTerm();
                roomLeaseTerm.setRoomId(roomSubmitVo.getId());
                roomLeaseTerm.setLeaseTermId(leaseTermId);
                roomLeaseTermList.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTermList);
        }
    }
}
