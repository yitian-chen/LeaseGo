package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zju.lease.model.entity.BrowsingHistory;
import com.zju.lease.web.app.mapper.BrowsingHistoryMapper;
import com.zju.lease.web.app.service.BrowsingHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.web.app.vo.history.HistoryItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author liubo
 * @description 针对表【browsing_history(浏览历史)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {

    @Autowired
    private BrowsingHistoryMapper browsingHistoryMapper;

    @Override
    public IPage<HistoryItemVo> pageItemByUserId(Page<HistoryItemVo> page, Long userId) {
        return browsingHistoryMapper.pageItemByUserId(page, userId);
    }

    @Override
    @Async
    public void saveHistory(Long userId, Long id) {
        // 保存或更新浏览记录
        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId, userId);
        queryWrapper.eq(BrowsingHistory::getRoomId, id);
        BrowsingHistory browsingHistory = browsingHistoryMapper.selectOne(queryWrapper);

        if (browsingHistory != null) {
            // 不为空，只需更新上次浏览时间
            browsingHistory.setBrowseTime(new Date());
            browsingHistoryMapper.updateById(browsingHistory);
        } else {
            BrowsingHistory newHistory = new BrowsingHistory();
            newHistory.setBrowseTime(new Date());
            newHistory.setUserId(userId);
            newHistory.setRoomId(id);
            browsingHistoryMapper.insert(newHistory);
        }
    }
}