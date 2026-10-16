package com.PiliPili.service.impl;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.pojo.VideoBarrage;
import com.PiliPili.entity.query.VideoBarrageQuery;
import com.PiliPili.mapper.VideoBarrageMapper;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 弹幕分片缓存集成测试（读路径）
 * <p>
 * 测试策略与 VideoInfoCacheTest 保持一致：
 * - MySQL：Mockito mock 掉 VideoBarrageMapper，用 verify 次数断言"回源了几次"
 * - Redis：用【真实】Redis（分片 key、空占位、SETNX 锁、TTL 抖动都依赖真实语义，mock 不出来）
 * - 本地没有 Redis 时整类自动跳过
 * <p>
 * 运行方式：
 * mvn test -DskipTests=false -Dtest=VideoBarrageCacheTest -pl PiliPili-common
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("弹幕分片缓存：分片回源 + 三件套 + 增量拉取")
class VideoBarrageCacheTest {

    /** fileId 长度 varchar(20) */
    private static final String FILE_ID = "TESTDANMU001";
    private static final String EMPTY_FILE_ID = "TESTDANMU002";

    @Mock
    private VideoBarrageMapper videoBarrageMapper;

    private static LettuceConnectionFactory connectionFactory;
    private static RedisTemplate<String, Object> redisTemplate;
    private static RedisUtils<Object> redisUtils;

    private RedisComponent redisComponent;
    private VideoBarrageServiceImpl barrageService;

    @BeforeAll
    static void requireRedis() {
        boolean reachable;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 6379), 800);
            reachable = true;
        } catch (Exception e) {
            reachable = false;
        }
        Assumptions.assumeTrue(reachable, "本地 Redis(127.0.0.1:6379) 未启动，跳过弹幕缓存集成测试");

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 复用线上序列化器，保证测的就是线上行为
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
        redisComponent = new RedisComponent();
        ReflectionTestUtils.setField(redisComponent, "redisUtils", redisUtils);

        barrageService = new VideoBarrageServiceImpl();
        ReflectionTestUtils.setField(barrageService, "redisComponent", redisComponent);
        ReflectionTestUtils.setField(barrageService, "videoBarrageMapper", videoBarrageMapper);

        cleanCache();
    }

    // ==================================================================
    // 1. 分片回源 + 回填
    // ==================================================================

    @Test
    @DisplayName("首次读取回源并回填分片；第二次读取直接命中 Redis")
    void sliceCache_firstReadBacksDb_secondReadHitsCache() {
        mockDb(Arrays.asList(
                buildBarrage(1, 10, "第一条"),
                buildBarrage(2, 20, "第二条")));

        List<VideoBarrage> first = barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID));
        List<VideoBarrage> second = barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID));

        assertThat(first).hasSize(2);
        assertThat(second).hasSize(2);
        assertThat(second.get(0).getText()).isEqualTo("第一条");

        // ★ 关键断言：两次请求，数据库只回源一次
        verify(videoBarrageMapper, times(1)).SelectByFileIdAndTimeRange(eq(FILE_ID), anyInt(), anyInt());
        verify(videoBarrageMapper, times(1)).SelectMaxTimeByFileId(FILE_ID);

        // 索引 + 内容 + 最大时间 都已落缓存
        assertThat(redisUtils.keyExists(RedisComponent.danmuIndexKey(FILE_ID, 0))).isTrue();
        assertThat(redisUtils.keyExists(RedisComponent.danmuDataKey(FILE_ID, 0))).isTrue();
        assertThat(redisUtils.keyExists(Constants.REDIS_KEY_DANMU_MAX_TIME + FILE_ID)).isTrue();

        // TTL 应为 24h + 0~10 分钟抖动（防雪崩）
        Long ttlSeconds = redisUtils.getExpireSeconds(RedisComponent.danmuDataKey(FILE_ID, 0));
        assertThat(ttlSeconds).isNotNull()
                .isGreaterThan(Constants.DANMU_CACHE_BASE_SECONDS - 5)
                .isLessThanOrEqualTo(Constants.DANMU_CACHE_BASE_SECONDS + Constants.DANMU_CACHE_RANDOM_SECONDS + 5);
    }

    @Test
    @DisplayName("分片隔离：只加载播放需要的分片，不会把整条视频的弹幕都拉出来")
    void sliceCache_onlyRequestedSlicesAreLoaded() {
        // time=10 落在分片 0；time=700 落在分片 2
        mockDb(Arrays.asList(
                buildBarrage(1, 10, "第0片"),
                buildBarrage(2, 700, "第2片")));

        int slice0 = RedisComponent.calcDanmuSlice(10);
        int slice2 = RedisComponent.calcDanmuSlice(700);
        assertThat(slice0).isEqualTo(0);
        assertThat(slice2).isEqualTo(2);

        // 只读分片 0
        List<VideoBarrage> slice0List = barrageService.GetVideoBarrageIncrement(FILE_ID, 0, 0, 300, 100);
        assertThat(slice0List).hasSize(1);
        assertThat(slice0List.get(0).getText()).isEqualTo("第0片");

        // 分片 2 此时不应被回源/缓存
        assertThat(redisUtils.keyExists(RedisComponent.danmuIndexKey(FILE_ID, slice2))).isFalse();
        verify(videoBarrageMapper, times(1)).SelectByFileIdAndTimeRange(eq(FILE_ID), eq(0), eq(300));
        verify(videoBarrageMapper, times(0)).SelectByFileIdAndTimeRange(eq(FILE_ID), eq(600), eq(900));
    }

    // ==================================================================
    // 2. 防穿透：空分片占位
    // ==================================================================

    @Test
    @DisplayName("防穿透：空分片写 60s 占位，连续 5 次请求只回源 1 次")
    void penetration_emptySliceCachedWithShortTtl() {
        // 注意：真实 SQL 里 max(time) 查不到数据返回 NULL，而 Mockito 对 Integer 默认返回 0，
        // 所以这里必须显式 stub 成 null，才能模拟"该视频一条弹幕都没有"的场景
        lenient().when(videoBarrageMapper.SelectMaxTimeByFileId(EMPTY_FILE_ID)).thenReturn(null);
        lenient().when(videoBarrageMapper.SelectByFileIdAndTimeRange(eq(EMPTY_FILE_ID), anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            assertThat(barrageService.GetVideoBarrageByVideoId(buildQuery(EMPTY_FILE_ID))).isEmpty();
        }

        verify(videoBarrageMapper, times(1)).SelectMaxTimeByFileId(EMPTY_FILE_ID);
        assertThat(redisUtils.keyExists(Constants.REDIS_KEY_DANMU_MAX_TIME + EMPTY_FILE_ID)).isTrue();

        // -1 是"该分P确认没有弹幕"的哨兵值（防穿透）
        Integer maxTime = redisComponent.getDanmuMaxTime(EMPTY_FILE_ID);
        assertThat(maxTime).isEqualTo(-1);
    }

    @Test
    @DisplayName("防穿透：分片索引为空时写空分片占位，TTL 明显短于 24h")
    void penetration_emptySliceMarkerHasShortTtl() {
        // 先让 maxTime 命中缓存（避免 max() 查询干扰断言），再回源一个空分片
        redisComponent.saveDanmuMaxTime(FILE_ID, 100);
        lenient().when(videoBarrageMapper.SelectByFileIdAndTimeRange(eq(FILE_ID), anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());

        barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID));
        barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID));

        verify(videoBarrageMapper, times(1)).SelectByFileIdAndTimeRange(eq(FILE_ID), eq(0), eq(300));
        String emptyKey = RedisComponent.danmuEmptyKey(FILE_ID, 0);
        assertThat(redisUtils.keyExists(emptyKey)).isTrue();
        Long ttlSeconds = redisUtils.getExpireSeconds(emptyKey);
        assertThat(ttlSeconds).isNotNull()
                .isGreaterThan(0L)
                .isLessThanOrEqualTo((long) Constants.DANMU_EMPTY_CACHE_SECONDS);
    }

    // ==================================================================
    // 3. 防击穿：分片级互斥锁
    // ==================================================================

    @Test
    @DisplayName("防击穿：50 并发读同一个未缓存分片，数据库只回源 1 次")
    void breakdown_50ConcurrentReadsShouldHitDbOnce() throws Exception {
        final AtomicInteger dbCalls = new AtomicInteger(0);
        lenient().when(videoBarrageMapper.SelectMaxTimeByFileId(FILE_ID)).thenReturn(100);
        lenient().when(videoBarrageMapper.SelectByFileIdAndTimeRange(eq(FILE_ID), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    dbCalls.incrementAndGet();
                    // 模拟真实查库耗时，制造"缓存重建窗口"
                    Thread.sleep(60);
                    return new ArrayList<>(Arrays.asList(buildBarrage(1, 10, "并发弹幕")));
                });

        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<List<VideoBarrage>> results = java.util.Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threads; i++) {
            pool.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    results.add(barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID)));
                } catch (Exception e) {
                    // 忽略，下面用数量断言兜底
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        // ★ 关键断言：50 个并发请求，只回源了 1 次
        assertThat(dbCalls.get()).isEqualTo(1);
        assertThat(results).hasSize(threads);
        for (List<VideoBarrage> list : results) {
            assertThat(list).hasSize(1);
        }
        // 锁必须已被释放
        assertThat(redisUtils.keyExists(RedisComponent.danmuLockKey(FILE_ID, 0))).isFalse();
    }

    // ==================================================================
    // 4. 增量拉取：(时间, ID) 游标不漏不重
    // ==================================================================

    @Test
    @DisplayName("增量拉取：同一秒多条弹幕，游标 (time,id) 能逐条推进且不丢不重")
    void increment_sameSecondMustNotBeLost() {
        // 同一秒(30s)有 3 条，ID 递增
        mockDb(Arrays.asList(
                buildBarrage(101, 30, "同秒1"),
                buildBarrage(102, 30, "同秒2"),
                buildBarrage(103, 30, "同秒3"),
                buildBarrage(104, 31, "下一秒")));

        // 先全量加载一次（回源填缓存）
        assertThat(barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID))).hasSize(4);

        // 客户端游标：已经消费到 (30, 101)，应只拿到剩下 3 条
        List<VideoBarrage> inc1 = barrageService.GetVideoBarrageIncrement(FILE_ID, 30, 101, 300, 100);
        assertThat(inc1).extracting(VideoBarrage::getText).containsExactly("同秒2", "同秒3", "下一秒");

        // 继续推进到 (31, 104)：没有新弹幕了
        List<VideoBarrage> inc2 = barrageService.GetVideoBarrageIncrement(FILE_ID, 31, 104, 300, 100);
        assertThat(inc2).isEmpty();

        // 从 0 开始拉：应拿到全部 4 条（按 time,id 升序）
        List<VideoBarrage> inc3 = barrageService.GetVideoBarrageIncrement(FILE_ID, 0, 0, 300, 100);
        assertThat(inc3).extracting(VideoBarrage::getText)
                .containsExactly("同秒1", "同秒2", "同秒3", "下一秒");
    }

    @Test
    @DisplayName("增量拉取：limit 生效，单次不会返回超过上限的弹幕")
    void increment_limitShouldApply() {
        List<VideoBarrage> data = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            data.add(buildBarrage(i, i, "第" + i + "条"));
        }
        mockDb(data);

        List<VideoBarrage> limited = barrageService.GetVideoBarrageIncrement(FILE_ID, 0, 0, 300, 5);
        assertThat(limited).hasSize(5);
        assertThat(limited.get(0).getText()).isEqualTo("第1条");
    }

    // ==================================================================
    // 5. 写后失效：新增/删除弹幕后分片必须被清掉
    // ==================================================================

    @Test
    @DisplayName("写后失效：删除分片缓存后，下次读取会重新回源")
    void invalidation_afterDeleteSliceShouldReloadFromDb() {
        mockDb(Arrays.asList(buildBarrage(1, 10, "旧弹幕")));

        assertThat(barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID))).hasSize(1);

        // 模拟"新增了一条弹幕"→ 失效该分片
        redisComponent.deleteDanmuSlice(FILE_ID, 0);
        redisComponent.deleteDanmuMaxTime(FILE_ID);

        mockDb(Arrays.asList(
                buildBarrage(1, 10, "旧弹幕"),
                buildBarrage(2, 11, "新弹幕")));

        List<VideoBarrage> reloaded = barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID));
        assertThat(reloaded).hasSize(2);
        verify(videoBarrageMapper, times(2)).SelectByFileIdAndTimeRange(eq(FILE_ID), anyInt(), anyInt());
    }

    @Test
    @DisplayName("clearDanmuCacheAllSlices：删除视频时能清空所有分片与索引")
    void deleteAllSlices_shouldRemoveEveryKey() {
        mockDb(Arrays.asList(buildBarrage(1, 10, "a"), buildBarrage(2, 700, "b")));
        barrageService.GetVideoBarrageByVideoId(buildQuery(FILE_ID));
        assertThat(redisUtils.keyExists(RedisComponent.danmuIndexKey(FILE_ID, 0))).isTrue();
        assertThat(redisUtils.keyExists(RedisComponent.danmuIndexKey(FILE_ID, 2))).isTrue();

        redisComponent.deleteDanmuCacheAllSlices(FILE_ID);

        assertThat(redisUtils.getByKeyPrefix(Constants.REDIS_KEY_DANMU_INDEX + FILE_ID + ":")).isEmpty();
        assertThat(redisUtils.getByKeyPrefix(Constants.REDIS_KEY_DANMU_DATA + FILE_ID + ":")).isEmpty();
        assertThat(redisUtils.keyExists(Constants.REDIS_KEY_DANMU_MAX_TIME + FILE_ID)).isFalse();
    }

    // ==================================================================
    // 辅助方法
    // ==================================================================

    /**
     * mock 一个"像真数据库一样"的 mapper：按 [timeMin,timeMax) 过滤 fixture
     */
    private void mockDb(List<VideoBarrage> fixture) {
        lenient().when(videoBarrageMapper.SelectMaxTimeByFileId(FILE_ID))
                .thenReturn(fixture.stream().map(VideoBarrage::getTime).max(Integer::compareTo).orElse(null));
        lenient().when(videoBarrageMapper.SelectByFileIdAndTimeRange(eq(FILE_ID), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    int timeMin = invocation.getArgument(1);
                    int timeMax = invocation.getArgument(2);
                    List<VideoBarrage> rows = new ArrayList<>();
                    for (VideoBarrage barrage : fixture) {
                        if (barrage.getTime() >= timeMin && barrage.getTime() < timeMax) {
                            rows.add(barrage);
                        }
                    }
                    return rows;
                });
    }

    private VideoBarrageQuery buildQuery(String fileId) {
        VideoBarrageQuery query = new VideoBarrageQuery();
        query.setFileId(fileId);
        query.setVideoId("TESTVIDEO1");
        return query;
    }

    private VideoBarrage buildBarrage(int id, int time, String text) {
        VideoBarrage barrage = new VideoBarrage();
        barrage.setBarrageId(id);
        barrage.setVideoId("TESTVIDEO1");
        barrage.setFileId(FILE_ID);
        barrage.setUserId("TESTUSER01");
        barrage.setTime(time);
        barrage.setText(text);
        barrage.setMode(0);
        barrage.setColor("#FFFFFF");
        barrage.setPostTime(LocalDateTime.now());
        return barrage;
    }

    /**
     * 清理测试产生的 key（含分片索引/内容/空占位/锁/最大时间）
     */
    private void cleanCache() {
        for (String fileId : new String[]{FILE_ID, EMPTY_FILE_ID}) {
            redisComponent.deleteDanmuCacheAllSlices(fileId);
        }
    }
}
