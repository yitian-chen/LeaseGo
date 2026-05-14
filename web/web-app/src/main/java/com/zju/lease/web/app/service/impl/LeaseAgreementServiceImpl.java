package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zju.lease.common.exception.LeaseException;
import com.zju.lease.common.result.ResultCodeEnum;
import com.zju.lease.model.entity.*;
import com.zju.lease.model.enums.ItemType;
import com.zju.lease.model.enums.LeaseStatus;
import com.zju.lease.web.app.mapper.*;
import com.zju.lease.web.app.service.LeaseAgreementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.web.app.vo.agreement.AgreementDetailVo;
import com.zju.lease.web.app.vo.agreement.AgreementItemVo;
import com.zju.lease.web.app.vo.graph.GraphVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean saveOrUpdate(LeaseAgreement entity) {
        if (entity.getRoomId() == null) {
            return super.saveOrUpdate(entity);
        }
        String lockKey = "lock:lease:room:" + entity.getRoomId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 业务校验：检查时间段是否与已有租约冲突
                if (entity.getId() == null || isRoomLeaseChanged(entity)) {
                    if (entity.getLeaseStartDate() == null || entity.getLeaseEndDate() == null) {
                        throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
                    }
                    List<LeaseAgreement> conflictLeases = leaseAgreementMapper.selectList(
                            new LambdaQueryWrapper<LeaseAgreement>()
                                    .eq(LeaseAgreement::getRoomId, entity.getRoomId())
                                    .eq(LeaseAgreement::getIsDeleted, 0)
                                    .notIn(LeaseAgreement::getStatus, LeaseStatus.EXPIRED, LeaseStatus.CANCELED, LeaseStatus.WITHDRAWN)
                                    .le(LeaseAgreement::getLeaseStartDate, entity.getLeaseEndDate())
                                    .ge(LeaseAgreement::getLeaseEndDate, entity.getLeaseStartDate())
                    );
                    if (!conflictLeases.isEmpty()) {
                        throw new LeaseException(ResultCodeEnum.ROOM_LEASE_ALREADY_EXISTS);
                    }
                }
                return super.saveOrUpdate(entity);
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean isRoomLeaseChanged(LeaseAgreement entity) {
        LeaseAgreement old = leaseAgreementMapper.selectById(entity.getId());
        return old == null || !old.getRoomId().equals(entity.getRoomId());
    }

    @Override
    public List<AgreementItemVo> listItemByPhone(String phone) {
        return leaseAgreementMapper.listItemByPhone(phone);
    }

    @Override
    public AgreementDetailVo getDetailById(Long id) {
        //1.查询租约信息
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if (leaseAgreement == null) {
            return null;
        }
        //2.查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());

        //3.查询房间信息
        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());

        //4.查询图片信息
        List<GraphVo> roomGraphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, leaseAgreement.getRoomId());
        List<GraphVo> apartmentGraphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, leaseAgreement.getApartmentId());

        //5.查询支付方式
        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());

        //6.查询租期
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());

        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);
        agreementDetailVo.setApartmentName(apartmentInfo.getName());
        agreementDetailVo.setRoomNumber(roomInfo.getRoomNumber());
        agreementDetailVo.setApartmentGraphVoList(apartmentGraphVoList);
        agreementDetailVo.setRoomGraphVoList(roomGraphVoList);
        agreementDetailVo.setPaymentTypeName(paymentType.getName());
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());

        return agreementDetailVo;
    }
}




