package com.PiliPili.service.impl;

import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.mapper.VideoInfoMapper;
import com.PiliPili.redis.RedisConfig;
import com.PiliPili.redis.RedisUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 视频详情缓存集成测试
 * <p>
 * 测试策略：
 * - MySQL：用 Mockito mock 掉 VideoInfoMapper（不依赖数据库），通过 verify 次数断言"回源了几次"
 * - Redis：用【真实】Redis 实例（缓存行为依赖 SETNX/Lua/TTL 语义，mock 不出来）
 * - 若本地 Redis 未启动，整个测试类自动跳过（不报错）
 * <p>
 * 运行方式（父 pom 配置了 skipTests=true，必须显式关闭）：
 * mvn test -DskipTests=false -Dtest=VideoInfoCacheTest -pl PiliPili-common
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("视频详情缓存：缓存三件套 + 失效 + 计数")
class VideoInfoCacheTest {

    /** 视频 id 长度必须是 varchar(10)，这里统一用 10 位测试 id */
    private static final String TEST_ID = "TESTCACHE1";
    private static final String UNKNOWN_ID = "NOTEXIST99";
    private static final String[] JITTER_IDS = {
            "TESTJIT001", "TESTJIT002", "TESTJIT003", "TESTJIT004",
            "TESTJIT005", "TESTJIT006", "TESTJIT007", "TESTJIT008"
    };

    @Mock
    private VideoInfoMapper videoInfoMapper;

    private static LettuceConnectionFactory connectionFactory;
    private static RedisTemplate<String, Object> redisTemplate;
    private static RedisUtils<Object> redisUtils;

    private VideoInfoServiceImpl videoInfoService;

    /**
     * 本地没有 Redis 就跳过整个测试类（避免在别人机器上跑红）
     */
    @BeforeAll
    static void requireRedis() {
        boolean reachable;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 6379), 800);
            reachable = true;
        } catch (Exception e) {
            reachable = false;
        }
        Assumptions.assumeTrue(reachable, "本地 Redis(127.0.0.1:6379) 未启动，跳过视频详情缓存集成测试");

        // 与项目 RedisConfig 保持一致的序列化方式（key=String, value=JSON）
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // ★ 直接复用线上 RedisConfig 的序列化器，
        //   保证"测试验证的序列化行为" == "线上运行的"，否则测了等于白测
        redisTemplate.setValueSerializer(RedisConfig.buildJsonSerializer());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisConfig.buildJsonSerializer());
        redisTemplate.afterPropertiesSet();

        redisUtils = new RedisUtils<>();
        ReflectionTestUtils.setField(redisUtils, "redisTemplate", redisTemplate);
    }

    @AfterAll
    static void tearDown() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void setUp() {
        // 只注入被测方法用到的两个依赖，其余字段为 null 不影响
        videoInfoService = new VideoInfoServiceImpl();
        ReflectionTestUtils.setField(videoInfoService, "redisUtils", redisUtils);
        ReflectionTestUtils.setField(videoInfoService, "videoInfoMapper", videoInfoMapper);
        cleanCache();
    }

    // ==================================================================
    // 1. 基础：缓存命中
    // ==================================================================

    @Test
    @DisplayName("缓存命中：第二次读取直接走 Redis，不再查库")
    void cacheHit_secondReadShouldNotHitDb() {
        when(videoInfoMapper.GetVideoInfoByVideoId(TEST_ID)).thenReturn(buildVideo(TEST_ID));

        VideoInfo first = videoInfoService.GetVideoInfoByVideoId(TEST_ID);
        VideoInfo second = videoInfoService.GetVideoInfoByVideoId(TEST_ID);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(second.getVideoId()).isEqualTo(TEST_ID);

        // ★ 关键断言：两次请求，数据库只被查询 1 次
        verify(videoInfoMapper, times(1)).GetVideoInfoByVideoId(TEST_ID);
        assertThat(redisUtils.keyExists(Constants.REDIS_KEY_VIDEO_INFO + TEST_ID)).isTrue();
    }

    // ==================================================================
    // 2. 防穿透：空值缓存
    // ==================================================================

    @Test
    @DisplayName("防穿透：不存在的 id 也缓存空值，3 次请求只查库 1 次")
    void penetration_unknownIdShouldBeCachedAsEmpty() {
        when(videoInfoMapper.GetVideoInfoByVideoId(UNKNOWN_ID)).thenReturn(null);

        assertThat(videoInfoService.GetVideoInfoByVideoId(UNKNOWN_ID)).isNull();
        assertThat(videoInfoService.GetVideoInfoByVideoId(UNKNOWN_ID)).isNull();
        assertThat(videoInfoService.GetVideoInfoByVideoId(UNKNOWN_ID)).isNull();

        // ★ 3 次请求（模拟恶意刷不存在的 id），数据库只被打了 1 次
        verify(videoInfoMapper, times(1)).GetVideoInfoByVideoId(UNKNOWN_ID);

        // 且写入了"空值占位 key"
        assertThat(redisUtils.keyExists(Constants.REDIS_KEY_VIDEO_INFO + UNKNOWN_ID)).isTrue();

        // 空值缓存的 TTL 应明显短于正常缓存的 30 分钟
        Long ttl = redisUtils.getExpire(Constants.REDIS_KEY_VIDEO_INFO + UNKNOWN_ID);
        assertThat(ttl).isNotNull().isGreaterThan(0L)
                .isLessThanOrEqualTo((Constants.VIDEO_INFO_NULL_CACHE_SECONDS + 61) * 1000L);
    }

    // ==================================================================
    // 3. 防击穿：互斥锁（核心）
    // ==================================================================

    @Test
    @DisplayName("防击穿：50 个并发同时请求同一视频，数据库只被查询 1 次")
    void breakdown_50ConcurrentRequestsShouldHitDbOnlyOnce() throws Exception {
        int threadCount = 50;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        when(videoInfoMapper.GetVideoInfoByVideoId(TEST_ID)).thenAnswer(invocation -> {
            Thread.sleep(20);          // 模拟一次耗时查询，拉长竞态窗口
            return buildVideo(TEST_ID);
        });

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            pool.execute(() -> {
                try {
                    startGate.await();                       // 所有线程在这里等，一起放行
                    videoInfoService.GetVideoInfoByVideoId(TEST_ID);
                } catch (Exception ignored) {
                    // 单个线程失败不影响整体断言
                } finally {
                    endGate.countDown();
                }
            });
        }
        startGate.countDown();                                // 同时放行 50 个请求
        boolean allDone = endGate.await(20, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(allDone).as("并发请求应在超时前全部完成").isTrue();

        // ★★ 核心断言：50 个并发，数据库只被查询 1 次（互斥锁 + 双重检查生效）
        verify(videoInfoMapper, times(1)).GetVideoInfoByVideoId(TEST_ID);
    }

    // ==================================================================
    // 4. 防雪崩：TTL 随机抖动
    // ==================================================================

    @Test
    @DisplayName("防雪崩：批量写入的缓存 TTL 带随机抖动，不会同时过期")
    void avalanche_ttlShouldHaveRandomJitter() {
        for (String id : JITTER_IDS) {
            when(videoInfoMapper.GetVideoInfoByVideoId(id)).thenReturn(buildVideo(id));
            videoInfoService.GetVideoInfoByVideoId(id);
        }

        Set<Long> ttlSet = new HashSet<>();
        for (String id : JITTER_IDS) {
            Long ttl = redisUtils.getExpire(Constants.REDIS_KEY_VIDEO_INFO + id);
            assertThat(ttl).as("缓存 %s 的 TTL", id).isNotNull().isGreaterThan(0L);
            // TTL 应落在 [基础值, 基础值+抖动范围] 内
            assertThat(ttl).isBetween(
                    Constants.VIDEO_INFO_CACHE_BASE_SECONDS * 1000L - 2000L,
                    (Constants.VIDEO_INFO_CACHE_BASE_SECONDS + Constants.VIDEO_INFO_CACHE_RANDOM_SECONDS) * 1000L + 2000L);
            ttlSet.add(ttl);
        }

        // ★ 8 个 key 的 TTL 不应该完全相同（完全相同就是"同时过期"的雪崩隐患）
        assertThat(ttlSet.size())
                .as("TTL 应分散（抖动生效），而不是全部相同")
                .isGreaterThan(1);
    }

    // ==================================================================
    // 5. 缓存失效
    // ==================================================================

    @Test
    @DisplayName("缓存失效：删除缓存后，下次读取重新回源数据库")
    void invalidate_shouldReloadFromDb() {
        when(videoInfoMapper.GetVideoInfoByVideoId(TEST_ID)).thenReturn(buildVideo(TEST_ID));

        videoInfoService.GetVideoInfoByVideoId(TEST_ID);     // 第 1 次：回源
        videoInfoService.GetVideoInfoByVideoId(TEST_ID);     // 第 2 次：命中缓存
        verify(videoInfoMapper, times(1)).GetVideoInfoByVideoId(TEST_ID);

        // 模拟"视频信息变更" → 删除缓存
        videoInfoService.deleteVideoInfoCache(TEST_ID);
        assertThat(redisUtils.keyExists(Constants.REDIS_KEY_VIDEO_INFO + TEST_ID)).isFalse();

        videoInfoService.GetVideoInfoByVideoId(TEST_ID);     // 第 3 次：缓存已删，重新回源

        // ★ 删除缓存后必须重新查库（否则会读到脏数据）
        verify(videoInfoMapper, times(2)).GetVideoInfoByVideoId(TEST_ID);
    }

    // ==================================================================
    // 6. 播放量：就地更新，不重置 TTL
    // ==================================================================

    @Test
    @DisplayName("播放量：就地更新缓存中的计数字段，且不重置 TTL")
    void playCount_inPlaceUpdateShouldKeepTtl() {
        when(videoInfoMapper.GetVideoInfoByVideoId(TEST_ID)).thenReturn(buildVideo(TEST_ID));
        videoInfoService.GetVideoInfoByVideoId(TEST_ID);

        String cacheKey = Constants.REDIS_KEY_VIDEO_INFO + TEST_ID;
        Long ttlBefore = redisUtils.getExpire(cacheKey);

        // 模拟一次播放（内部会更新 MySQL 计数 + 就地更新缓存计数）
        videoInfoService.addVideoPlayCount(TEST_ID);

        VideoInfo cached = (VideoInfo) redisUtils.get(cacheKey);
        assertThat(cached).isNotNull();
        assertThat(cached.getPlayCount()).as("缓存里的播放量应 +1").isEqualTo(1);

        Long ttlAfter = redisUtils.getExpire(cacheKey);
        assertThat(ttlAfter).as("就地更新不应把 TTL 重置为更长时间").isNotNull()
                .isLessThanOrEqualTo(ttlBefore == null ? Long.MAX_VALUE : ttlBefore);
    }

    // ==================================================================
    // 辅助方法
    // ==================================================================

    private VideoInfo buildVideo(String videoId) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setVideoId(videoId);
        videoInfo.setVideoName("测试视频-" + videoId);
        videoInfo.setVideoCover("COVER/test/cover.png");
        videoInfo.setUserId("6598774110");
        videoInfo.setDuration(100);
        videoInfo.setPlayCount(0);
        // 真实视频数据里这两个字段是 LocalDateTime，
        // Jackson 序列化 Java8 时间类型需要注册 JavaTimeModule，这里必须覆盖到，
        // 否则线上会出现 "Java 8 date/time type not supported by default" 报错
        videoInfo.setCreateTime(LocalDateTime.now());
        videoInfo.setLastUpdateTime(LocalDateTime.now());
        return videoInfo;
    }

    /**
     * 清理测试用的缓存 key，避免脏数据影响断言
     */
    private void cleanCache() {
        deleteByPrefix(Constants.REDIS_KEY_VIDEO_INFO + "TEST");
        deleteByPrefix(Constants.REDIS_KEY_VIDEO_INFO + "NOTEXIST");
        deleteByPrefix(Constants.REDIS_KEY_VIDEO_INFO_LOCK + "TEST");
        deleteByPrefix(Constants.REDIS_KEY_VIDEO_INFO_LOCK + "NOTEXIST");
    }

    private void deleteByPrefix(String prefix) {
        Set<String> keys = redisUtils.getByKeyPrefix(prefix);
        if (keys != null && !keys.isEmpty()) {
            redisUtils.delete(keys.toArray(new String[0]));
        }
    }
}
