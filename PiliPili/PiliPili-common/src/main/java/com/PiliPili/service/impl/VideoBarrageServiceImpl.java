package com.PiliPili.service.impl;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.enums.SearchTypeOrderEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoBarrage;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.VideoBarrageQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.VideoBarrageMapper;
import com.PiliPili.mapper.VideoInfoMapper;
import com.PiliPili.service.VideoBarrageService;
import com.PiliPili.utils.StringToolsUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VideoBarrageServiceImpl implements VideoBarrageService {

    @Resource
    VideoBarrageMapper videoBarrageMapper;

    @Resource
    VideoInfoMapper videoInfoMapper;

    @Resource
    EsSearchComponent esSearchComponent;

    @Resource
    RedisComponent redisComponent;

    /**
     * 保存发送的弹幕
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void SaveVideoBarrage(VideoBarrage videoBarrage) {
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoBarrage.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(600, "参数错误");
        }
        //interaction字段包含1则说明视频关闭弹幕功能
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            throw new BusinessException("该视频已关闭弹幕功能");
        }
        this.videoBarrageMapper.InsertVideoBarrage(videoBarrage);
        //更新视频弹幕数
        this.videoInfoMapper.UpdateVideoCount(videoInfo.getVideoId(), 1, "barrage_count");
        //TODO 未测试 更新es 弹幕数
        esSearchComponent.UpdateDocCount(videoBarrage.getVideoId(), SearchTypeOrderEnum.VIDEO_BARRAGE.getField(), 1);

        // 缓存一致性：新弹幕落库后，让对应分片缓存失效（下次读请求重新回源）
        // 必须放在事务提交之后，否则其他线程可能读到"还没提交的数据"又把旧值写回缓存
        deleteDanmuCacheAfterCommit(videoBarrage.getFileId(), videoBarrage.getTime());
    }

    /**
     * 获取视频弹幕（Cache-Aside：按时间分片读 Redis，未命中回源 MySQL 并回填）
     * <p>
     * 为什么分片？
     * 一条长视频的弹幕可能有几万条，整条视频一个 key 就是 big key（阻塞、删除慢、分片不均）。
     * 按 5 分钟一片拆开后，单 key 只有几十 KB，而且播放时只需要加载"播放位置附近"的分片。
     * <p>
     * 为什么索引(ZSet)和内容(Hash)分开存？
     * ZSet 提供"按时间范围查、按 id 删"的能力（O(log N)），Hash 提供按 id 精确读写；
     * 若把弹幕 JSON 直接塞进 ZSet 的 member，删除单条弹幕就得全量扫描。
     */
    @Override
    public List<VideoBarrage> GetVideoBarrageByVideoId(VideoBarrageQuery videoBarrageQuery) {
        String fileId = videoBarrageQuery.getFileId();
        //没有 fileId（历史调用方式）→ 退回原 SQL，保证兼容
        if (StringToolsUtils.IsEmpty(fileId)) {
            return this.videoBarrageMapper.GetVideoBarrageByVideoId(videoBarrageQuery);
        }
        int maxTime = resolveDanmuMaxTime(fileId);
        if (maxTime < 0) {
            return new ArrayList<>();
        }
        int maxSlice = Math.min(RedisComponent.calcDanmuSlice(maxTime), Constants.DANMU_MAX_SLICE_LOAD - 1);
        List<VideoBarrage> result = new ArrayList<>();
        for (int slice = 0; slice <= maxSlice; slice++) {
            result.addAll(loadDanmuSlice(fileId, slice));
        }
        RedisComponent.sortDanmu(result);
        return result;
    }

    /**
     * 增量拉取弹幕：只取 (fromTime,fromId] ~ toTime 之间新增的部分
     * <p>
     * 游标必须带 ID：弹幕时间精确到秒，同一秒可能有多条，只用时间做游标会漏取或重复取。
     */
    @Override
    public List<VideoBarrage> GetVideoBarrageIncrement(String fileId, Integer fromTime, Integer fromId,
                                                       Integer toTime, Integer limit) {
        if (StringToolsUtils.IsEmpty(fileId)) {
            return new ArrayList<>();
        }
        int safeFromTime = fromTime == null || fromTime < 0 ? 0 : fromTime;
        int safeFromId = fromId == null ? 0 : fromId;
        int safeLimit = limit == null || limit <= 0 ? Constants.DANMU_INCREMENT_DEFAULT_LIMIT : limit;
        //不传 toTime 时默认只取一片的时间窗（5 分钟），避免长视频一次拉全量
        int safeToTime = toTime == null || toTime < safeFromTime ? safeFromTime + Constants.DANMU_SLICE_SECONDS : toTime;

        //先保证增量区间覆盖的分片已经就绪（未缓存的分片在这里回源 DB）
        int fromSlice = RedisComponent.calcDanmuSlice(safeFromTime);
        int toSlice = Math.min(RedisComponent.calcDanmuSlice(safeToTime), Constants.DANMU_MAX_SLICE_LOAD - 1);
        for (int slice = fromSlice; slice <= toSlice; slice++) {
            loadDanmuSlice(fileId, slice);
        }
        List<VideoBarrage> result = redisComponent.getDanmuIncrement(fileId, safeFromTime, safeFromId, safeToTime, safeLimit);
        //增量区间之外（toTime 之后）也可能存在更晚的弹幕，这里只返回区间内的，客户端按返回值推进游标即可
        return result;
    }

    /**
     * 加载一个分片：缓存命中直接返回；未命中则"抢锁 + 双重检查 + 回源 + 回填"
     */
    private List<VideoBarrage> loadDanmuSlice(String fileId, int slice) {
        // ① 先读缓存（null 表示没缓存，空集合表示已确认该分片没有弹幕）
        List<VideoBarrage> cached = redisComponent.tryGetDanmuSlice(fileId, slice);
        if (cached != null) {
            return cached;
        }
        // ② 抢分片锁，只放一个线程回源（防击穿）
        String lockValue = redisComponent.tryLockDanmuSlice(fileId, slice);
        if (lockValue == null) {
            // 没抢到：短暂等待别的线程回填，仍拿不到就降级直接查库
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                cached = redisComponent.tryGetDanmuSlice(fileId, slice);
                if (cached != null) {
                    return cached;
                }
            }
            log.warn("等待弹幕分片重建超时，降级直接查库,fileId:{},slice:{}", fileId, slice);
            return queryDanmuSliceFromDb(fileId, slice);
        }
        try {
            // ③ 双重检查：拿到锁后再看一眼缓存
            cached = redisComponent.tryGetDanmuSlice(fileId, slice);
            if (cached != null) {
                return cached;
            }
            // ④ 回源 DB
            List<VideoBarrage> dbList = queryDanmuSliceFromDb(fileId, slice);
            // ⑤ 回填缓存（空分片写"空占位"，TTL 24h + 随机抖动）
            redisComponent.saveDanmuSlice(fileId, slice, dbList);
            return dbList;
        } finally {
            // ⑥ 释放锁（Lua 校验持有凭证，防止误删别人的锁）
            redisComponent.unlockDanmuSlice(fileId, slice, lockValue);
        }
    }

    /**
     * 按分片的时间区间查库（左闭右开）
     */
    private List<VideoBarrage> queryDanmuSliceFromDb(String fileId, int slice) {
        try {
            return this.videoBarrageMapper.SelectByFileIdAndTimeRange(fileId,
                    RedisComponent.danmuSliceStart(slice), RedisComponent.danmuSliceEnd(slice));
        } catch (Exception e) {
            log.error("弹幕分片回源失败,fileId:{},slice:{}", fileId, slice, e);
            return new ArrayList<>();
        }
    }

    /**
     * 取该分P弹幕的最大时间（决定全量加载时扫描到哪一片）
     *
     * @return -1 表示没有任何弹幕
     */
    private int resolveDanmuMaxTime(String fileId) {
        Integer cached = redisComponent.getDanmuMaxTime(fileId);
        if (cached != null) {
            return cached;
        }
        Integer maxTime = this.videoBarrageMapper.SelectMaxTimeByFileId(fileId);
        int value = maxTime == null ? -1 : maxTime;
        redisComponent.saveDanmuMaxTime(fileId, value);
        return value;
    }

    /**
     * 失效弹幕分片缓存（新增/删除弹幕后调用）
     * <p>
     * 为什么"删缓存"而不是"更新缓存"：更新缓存在并发下会被旧值覆盖，
     * 删除更简单安全，下次读请求自然回源重建（与视频详情缓存保持一致策略）。
     */
    private void deleteDanmuCacheAfterCommit(String fileId, Integer time) {
        if (StringToolsUtils.IsEmpty(fileId)) {
            return;
        }
        final int slice = RedisComponent.calcDanmuSlice(time);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisComponent.deleteDanmuSlice(fileId, slice);
                    //弹幕最大时间也可能变大，一并失效
                    redisComponent.deleteDanmuMaxTime(fileId);
                }
            });
        } else {
            redisComponent.deleteDanmuSlice(fileId, slice);
            redisComponent.deleteDanmuMaxTime(fileId);
        }
    }

    /**
     * 查询弹幕
     */
    @Override
    public PageBean GetBarrageByParam(VideoBarrageQuery videoBarrageQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoBarrageQuery.getPageNumber(), videoBarrageQuery.getPageSize());
        Page<VideoBarrage> videoBarragePage = (Page<VideoBarrage>) this.videoBarrageMapper.SelectByParam(videoBarrageQuery);
        pageBean.setPageSize(videoBarrageQuery.getPageSize());
        pageBean.setPageNum(videoBarrageQuery.getPageNumber());
        pageBean.setPageTotal(videoBarragePage.getPages());
        pageBean.setRows(videoBarragePage.getResult());
        pageBean.setTotal(videoBarragePage.getTotal());
        return pageBean;
    }

    /**
     * 删除弹幕（缓存同步失效）
     */
    @Override
    public void DeleteBarrage(String userId, Integer barrageId) {
        VideoBarrage videoBarrage = this.videoBarrageMapper.SelectByBarrageId(barrageId);
        if (videoBarrage == null) {
            throw new BusinessException("参数错误");
        }
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoBarrage.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException("参数错误");
        }
        if (userId != null && !videoInfo.getUserId().equals(userId)) {
            throw new BusinessException("参数错误");
        }
        this.videoBarrageMapper.DeleteByBarrageId(barrageId);
        //删除后同步失效该弹幕所在分片
        redisComponent.deleteDanmuSlice(videoBarrage.getFileId(), RedisComponent.calcDanmuSlice(videoBarrage.getTime()));
    }
}
