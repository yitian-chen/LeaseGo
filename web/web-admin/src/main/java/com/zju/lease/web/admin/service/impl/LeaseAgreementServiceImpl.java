package com.zju.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zju.lease.common.exception.LeaseException;
import com.zju.lease.common.result.ResultCodeEnum;
import com.zju.lease.model.entity.*;
import com.zju.lease.model.enums.LeaseStatus;
import com.zju.lease.web.admin.mapper.*;
import com.zju.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.zju.lease.web.admin.vo.agreement.AgreementVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 分布式锁 + 业务校验：防止同一房间被重复签约
     */
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

    /** 更新时如果 roomId 变了，需要重新校验 */
    private boolean isRoomLeaseChanged(LeaseAgreement entity) {
        LeaseAgreement old = leaseAgreementMapper.selectById(entity.getId());
        return old == null || !old.getRoomId().equals(entity.getRoomId());
    }

    @Override
    public AgreementVo getAgreementById(Long id) {
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);

        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());

        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());

        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getLeaseTermId());

        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());

        AgreementVo agreementVo = new AgreementVo();
        BeanUtils.copyProperties(leaseAgreement, agreementVo);
        agreementVo.setApartmentInfo(apartmentInfo);
        agreementVo.setRoomInfo(roomInfo);
        agreementVo.setPaymentType(paymentType);
        agreementVo.setLeaseTerm(leaseTerm);

        return agreementVo;
    }

    @Override
    public IPage<AgreementVo> pageAgreement(Page<AgreementVo> page, AgreementQueryVo queryVo) {
        return leaseAgreementMapper.pageAgreement(page, queryVo);
    }
}
