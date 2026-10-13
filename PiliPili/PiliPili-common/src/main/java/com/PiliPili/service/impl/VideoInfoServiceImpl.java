package com.PiliPili.service.impl;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.SysSettingDto;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.query.*;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.*;
import com.PiliPili.redis.RedisUtils;
import com.PiliPili.service.VideoInfoService;
import com.PiliPili.utils.StringToolsUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Service
@Slf4j
public class VideoInfoServiceImpl implements VideoInfoService {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserMapper userMapper;
    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private VideoInfoMapper videoInfoMapper;

    @Resource
    private VideoInfoFileMapper videoInfoFileMapper;

    @Resource
    private VideoInfoPostMapper videoInfoPostMapper;

    @Resource
    private VideoInfoFilePostMapper videoInfoFilePostMapper;

    @Resource
    private VideoBarrageMapper videoBarrageMapper;

    @Resource
    private VideoCommentMapper videoCommentMapper;

    /**
     * 获取推荐视频
     */
    @Override
    public List<VideoInfo> GetRecommendVideo(VideoInfoQuery videoInfoQuery) {
        return this.videoInfoMapper.GetRecommendVideo(videoInfoQuery);
    }

    /**
     * 获取分类视频
     *
     * @param videoInfoQuery
     */
    @Override
    public PageBean GetVideo(VideoInfoQuery videoInfoQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoInfoQuery.getPageNumber(), videoInfoQuery.getPageSize());
        List<VideoInfo> videoInfoList = this.videoInfoMapper.GetVideo(videoInfoQuery);
        PageInfo<VideoInfo> pageList = new PageInfo<>(videoInfoList);
        pageBean.setRows(pageList.getList());
        pageBean.setTotal(pageList.getTotal());
        pageBean.setPageNum(videoInfoQuery.getPageNumber());
        pageBean.setPageSize(videoInfoQuery.getPageSize());
        pageBean.setPageTotal(pageList.getPages());
        return pageBean;
    }

    /**
     * 根据视频id获取视频信息（Cache-Aside 旁路缓存模式）
     * <p>
     * 缓存三件套防护：
     * ① 防穿透：查库为空时，写入"空值占位对象"（短 TTL），恶意随机 id 不再打库
     * ② 防击穿：缓存未命中时用 Redis 分布式锁，同一时刻只放一个线程回源重建
     * ③ 防雪崩：TTL 加随机抖动，避免批量 key 在同一时刻集体过期
     *
     * @param videoId
     */
    @Override
    public VideoInfo GetVideoInfoByVideoId(String videoId) {
        if (StringToolsUtils.IsEmpty(videoId)) {
            return null;
        }
        String cacheKey = Constants.REDIS_KEY_VIDEO_INFO + videoId;

        // ========== ① 先查缓存 ==========
        VideoInfo cacheInfo = (VideoInfo) redisUtils.get(cacheKey);
        if (cacheInfo != null) {
            // 占位对象的 videoId 为空 → 说明之前已确认数据库里不存在（防穿透命中），直接返回 null
            return isNullHolder(cacheInfo) ? null : cacheInfo;
        }

        // ========== ② 未命中 → 抢锁，只放一个线程回源（防击穿）==========
        String lockKey = Constants.REDIS_KEY_VIDEO_INFO_LOCK + videoId;
        String lockValue = UUID.randomUUID().toString();
        boolean locked = redisUtils.setnx(lockKey, lockValue, Constants.VIDEO_INFO_LOCK_SECONDS * 1000L);
        if (!locked) {
            // 没抢到锁：说明已有线程在重建缓存，短暂等待后重试读缓存，而不是也去查库
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                VideoInfo retry = (VideoInfo) redisUtils.get(cacheKey);
                if (retry != null) {
                    return isNullHolder(retry) ? null : retry;
                }
            }
            // 还是等不到：降级直接查库（可用性优先，宁可慢一点也不报错）
            log.warn("等待视频缓存重建超时，降级直接查库,videoId:{}", videoId);
            return this.videoInfoMapper.GetVideoInfoByVideoId(videoId);
        }

        try {
            // ========== ③ 双重检查：拿到锁后再看一眼缓存，避免重复回源 ==========
            VideoInfo again = (VideoInfo) redisUtils.get(cacheKey);
            if (again != null) {
                return isNullHolder(again) ? null : again;
            }

            // ========== ④ 回源查库 ==========
            VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoId);
            if (videoInfo == null) {
                // 防穿透：把"不存在"也缓存起来（短 TTL），避免恶意 id 反复打库
                long nullTtl = randomSeconds(Constants.VIDEO_INFO_NULL_CACHE_SECONDS, 60);
                redisUtils.setex(cacheKey, new VideoInfo(), nullTtl * 1000L);
                return null;
            }

            // ========== ⑤ 回写缓存（TTL 带随机抖动，防雪崩）==========
            long ttl = randomSeconds(Constants.VIDEO_INFO_CACHE_BASE_SECONDS, Constants.VIDEO_INFO_CACHE_RANDOM_SECONDS);
            redisUtils.setex(cacheKey, videoInfo, ttl * 1000L);
            log.info("视频详情缓存回源成功,videoId:{},ttl:{}秒", videoId, ttl);
            return videoInfo;
        } finally {
            // ========== ⑥ 释放锁（Lua 校验值，防止误删别人的锁）==========
            redisUtils.releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 判断是否为"空值占位对象"（防穿透用）
     * 正常的视频信息 videoId 一定不为空，只有占位对象才会是空
     */
    private boolean isNullHolder(VideoInfo cacheInfo) {
        return StringToolsUtils.IsEmpty(cacheInfo.getVideoId());
    }

    /**
     * 删除视频详情缓存（视频信息变更 / 审核通过 / 删除视频时调用）
     * <p>
     * 两个设计要点：
     * ① 为什么"删除"而不是"更新缓存"？
     * 更新缓存存在并发覆盖问题（两个线程先后写，可能把旧值盖回去），
     * 删除更简单安全，下次读请求自然回源重建。
     * ② 为什么要在事务提交后才删？
     * 如果事务还没提交就删了缓存，其他线程此时来读会读到"旧数据"并重新写进缓存，
     * 脏数据就会长期存在。更严格的做法是"延迟双删"（提交后再延迟删一次）或订阅 binlog 异步失效。
     */
    @Override
    public void deleteVideoInfoCache(String videoId) {
        if (StringToolsUtils.IsEmpty(videoId)) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 当前处于事务中 → 延迟到事务提交成功后再删，保证"库已改完"才失效缓存
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doDeleteVideoInfoCache(videoId);
                }
            });
        } else {
            doDeleteVideoInfoCache(videoId);
        }
    }

    /**
     * 真正执行缓存删除（失败不影响主流程）
     */
    private void doDeleteVideoInfoCache(String videoId) {
        try {
            redisUtils.delete(Constants.REDIS_KEY_VIDEO_INFO + videoId);
            log.info("已删除视频详情缓存,videoId:{}", videoId);
        } catch (Exception e) {
            log.warn("删除视频详情缓存失败,videoId:{}", videoId, e);
        }
    }

    /**
     * 就地更新缓存中的视频字段（保留原有 TTL），缓存不存在则直接跳过
     * <p>
     * 用于播放量这类"变化极其频繁"的字段：如果每次变化都删缓存，缓存就形同虚设
     *
     * @param updater 对 VideoInfo 的修改逻辑
     */
    private void updateVideoInfoCacheField(String videoId, Consumer<VideoInfo> updater) {
        try {
            String cacheKey = Constants.REDIS_KEY_VIDEO_INFO + videoId;
            VideoInfo cacheInfo = (VideoInfo) redisUtils.get(cacheKey);
            if (cacheInfo == null || isNullHolder(cacheInfo)) {
                return;
            }
            updater.accept(cacheInfo);
            // 保留剩余 TTL，避免因就地更新而把缓存变成永久 key
            Long ttl = redisUtils.getExpire(cacheKey);
            if (ttl != null && ttl > 0) {
                redisUtils.setex(cacheKey, cacheInfo, ttl);
            } else {
                redisUtils.set(cacheKey, cacheInfo);
            }
        } catch (Exception e) {
            // 缓存更新失败绝不能影响主流程
            log.warn("就地更新视频缓存失败,videoId:{}", videoId, e);
        }
    }

    /**
     * 基础时间 + 随机抖动（防雪崩）
     */
    private long randomSeconds(int baseSeconds, int randomRangeSeconds) {
        return baseSeconds + ThreadLocalRandom.current().nextInt(randomRangeSeconds + 1);
    }

    /**
     * 获取视频列表
     *
     * @param videoInfoQuery
     * @return
     */
    @Override
    public PageBean GetVideoList(VideoInfoQuery videoInfoQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoInfoQuery.getPageNumber(), videoInfoQuery.getPageSize());
        List<VideoInfo> videoInfoList = this.videoInfoMapper.GetVideoList(videoInfoQuery);
        Page<VideoInfo> pageInfo = (Page<VideoInfo>) videoInfoList;
        pageBean.setRows(pageInfo.getResult());
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setPageSize(videoInfoQuery.getPageSize());
        pageBean.setPageNum(videoInfoQuery.getPageNumber());
        pageBean.setPageTotal(pageInfo.getPages());
        return pageBean;
    }

    /**
     * 获取该用户下的过滤过的视频
     *
     * @param videoInfoQuery
     * @return
     */
    @Override
    public List<VideoInfo> GetVideoListByUserId(VideoInfoQuery videoInfoQuery) {
        return this.videoInfoMapper.GetVideoList(videoInfoQuery);
    }

    /**
     * 获取分类下的视频数
     *
     * @param videoInfoQuery
     * @return
     */
    @Override
    public Integer GetVideoCount(VideoInfoQuery videoInfoQuery) {
        return this.videoInfoMapper.GetVideoCountByCategoryId(videoInfoQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ChangeInteraction(String videoId, String userId, String interaction) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setInteraction(interaction);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setUserId(userId);
        videoInfoMapper.UpdateByParam(videoInfo, videoInfoQuery);

        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setInteraction(interaction);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostQuery.setUserId(userId);
        videoInfoPostMapper.updateByParams(videoInfoPost, videoInfoPostQuery);

        // 视频信息变更 → 删除缓存（事务提交后）
        deleteVideoInfoCache(videoId);
    }

    /**
     * 删除视频
     *
     * @param videoId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void DeleteVideo(String videoId, String userId) {
        VideoInfo videoInfo = videoInfoMapper.GetVideoInfoByVideoId(videoId);

        if (videoInfo == null || userId != null && !userId.equals(videoInfo.getUserId())) {
            throw new BusinessException("参数错误");
        }

        this.videoInfoMapper.DeleteByVideoId(videoId);

        this.videoInfoPostMapper.DeleteByVideoId(videoId);

        // 视频已删除 → 删除缓存（事务提交后）
        deleteVideoInfoCache(videoId);

        SysSettingDto sysSettingDto = redisComponent.GetSysSettingInfo();
        this.userMapper.UpdateUserCount(videoInfo.getUserId(), -sysSettingDto.getPostVideoCoinCount(), "current_coin");

        //TODO 删除ES信息


        executorService.execute(() -> {
            VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
            videoInfoQuery.setVideoId(videoId);

            VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
            videoInfoFileQuery.setVideoId(videoId);
            //删除正式视频分p
            this.videoInfoFileMapper.deleteByVideoId(videoInfoFileQuery);

            VideoFilePostQuery videoFilePostQuery = new VideoFilePostQuery();
            videoFilePostQuery.setVideoId(videoId);
            //删除发布视频表分p
            this.videoInfoFilePostMapper.DeleteByVideoId(videoFilePostQuery);

            //删除弹幕
            VideoBarrageQuery videoBarrageQuery = new VideoBarrageQuery();
            videoBarrageQuery.setVideoId(videoId);
            this.videoBarrageMapper.DeleteByParam(videoBarrageQuery);

            //删除评论
            this.videoCommentMapper.DeleteCommentByVideoId(videoId);

            List<VideoInfoFile> videoInfoFileList = this.videoInfoFileMapper.GetVideoPListByVideoId(videoInfoQuery);
            for (VideoInfoFile videoInfoFile : videoInfoFileList) {
                try {
                    FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + videoInfoFile.getFilePath()));
                } catch (IOException e) {
                    log.error("删除文件失败，文件路径：{}", videoInfoFile.getFilePath());
                }
            }
        });
    }

    /**
     * 增加视频播放量
     * <p>
     * 播放量变化极其频繁（每次播放都触发），如果每次都删缓存，缓存就形同虚设，
     * 所以这里改为"就地更新缓存里的计数字段"并保留原 TTL。
     * 注意：这是 read-modify-write，并非原子操作，高并发下可能丢少量计数；
     * 但 MySQL 才是权威数据源，缓存 TTL 到期回源后会自动纠正。
     * 更严格的做法是把计数器单独用 Redis INCR 维护，读取时再合并展示。
     *
     * @param videoId
     */
    @Override
    public void addVideoPlayCount(String videoId) {
        this.videoInfoMapper.UpdateVideoCount(videoId, 1, ActionTypeEnum.VIDEO_PLAY.getField());
        updateVideoInfoCacheField(videoId, videoInfo ->
                videoInfo.setPlayCount((videoInfo.getPlayCount() == null ? 0 : videoInfo.getPlayCount()) + 1));
    }

    //TODO 播放历史 17：08 视频模块

    //TODO 删除视频 搜索热词 8：24
}
