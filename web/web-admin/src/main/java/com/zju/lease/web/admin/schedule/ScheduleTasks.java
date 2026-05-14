package com.zju.lease.web.admin.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zju.lease.common.rabbit.LeaseMessage;
import com.zju.lease.common.rabbit.RabbitMQConfig;
import com.zju.lease.model.entity.LeaseAgreement;
import com.zju.lease.model.enums.LeaseStatus;
import com.zju.lease.web.admin.mapper.UserInfoMapper;
import com.zju.lease.web.admin.service.LeaseAgreementService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ScheduleTasks {

    @Autowired
    private LeaseAgreementService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus() {
        // 分布式锁：防止多实例重复执行
        RLock lock = redissonClient.getLock("lock:task:checkLeaseStatus");
        if (!lock.tryLock()) {
            log.info("checkLeaseStatus skipped: lock held by another instance");
            return;
        }
        try {
        // 查询即将过期的租约
        List<LeaseAgreement> expiringLeases = service.list(new LambdaQueryWrapper<LeaseAgreement>()
                .le(LeaseAgreement::getLeaseEndDate, new Date())
                .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING));

        // 批量更新状态
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.le(LeaseAgreement::getLeaseEndDate, new Date());
        updateWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
        updateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);
        service.update(updateWrapper);

        // 发送到期通知
        for (LeaseAgreement lease : expiringLeases) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.LEASE_EXCHANGE, RabbitMQConfig.LEASE_EXPIRED_KEY,
                    new LeaseMessage(
                            lease.getId(),
                            lease.getRoomId(),
                            lease.getPhone(),
                            lease.getName(),
                            lease.getLeaseEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    ));
        }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
