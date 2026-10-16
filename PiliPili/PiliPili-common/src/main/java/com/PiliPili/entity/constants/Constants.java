package com.PiliPili.entity.constants;

public class Constants {

    public static Integer ZERO = 0;
    public static Integer ONE = 1;
    public static String TOKEN_WEB = "token";
    public static String TOKEN_ADMIN = "adminToken";
    public static Integer CATEGORY_LEVEL_ONE = 0;
    public static final int LENGTH_10 = 10;
    public static final int LENGTH_30 = 30;
    public static final Integer MB_SIZE = 1024 * 1024;
    public static final Integer UPDATE_USER_NAME_COIN = 5;
    public static final String DATE_FORMAT_1 = "yyyy-MM";
    public static final String DATE_FORMAT_2 = "yyyy-MM-dd";
    public static final String REG = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";
    public static final String FILE_FOLDER = "file/";
    public static final String FILE_TEMP = "temp/";
    public static final String FILE_VIDEO = "video/";
    public static final String FILE_COVER = "COVER/";
    public static final String THUMBNAIL_SUFFIX = "_thumbnail.jpg";
    public static final Integer REDIS_KEY_EXPIRE_TIME = 1000 * 60;
    public static final String REDIS_KEY_PREFIX = "PiliPili:";
    public static final String M3U8_FILE = "index.m3u8";
    public static final Integer REDIS_KEY_ONE_SECONDS = 1000;
    public static String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkCode:";
    public static String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX + "token:web:";
    public static String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREFIX + "token:admin:";
    public static String REDIS_KEY_CATEGORY_LIST = REDIS_KEY_PREFIX + "categoryList:";
    public static String REDIS_KEY_UPLOADING_FILE = REDIS_KEY_PREFIX + "uploading:";
    public static String REDIS_KEY_SYS_SETTING = REDIS_KEY_PREFIX + "sysSetting:";
    public static String REDIS_KEY_FILE_DEL = REDIS_KEY_PREFIX + "file:list:delete";
    public static String REDIS_KEY_FILE_INSERT = REDIS_KEY_PREFIX + "queue:transfer";
    public static String REDIS_KEY_QUEUE_VIDEO_PLAY = REDIS_KEY_PREFIX + "queue:video:play:";

    public static String REDIS_KEY_VIDEO_SEARCH_COUNT = REDIS_KEY_PREFIX + "video:search";

    //视频在线人数
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX = REDIS_KEY_PREFIX + "video:play:online:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX + "count:%s";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USE_PREFIX = "user:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USE = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX + REDIS_KEY_VIDEO_PLAY_COUNT_USE_PREFIX + "%s:%s";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT = REDIS_KEY_PREFIX + "video:playcount:";
    //视频合并临时文件
    public static final String TEMP_VIDEO_NAME = "/temp.mp4";
    public static final String VIDEO_CODE_HEVC = "hevc";
    public static final String VIDEO_CODE_TEMP_FILE = "_temp";

    public static final String TS_NAME = "index.ts";
    public static final String M3U8_NAME = "index.m3u8";

    //用户评分矩阵
    public static final String User_SCORE_MATRIX = REDIS_KEY_PREFIX + "userScoreMatrix";
    public static final String User_SCORE_AVERAGE = REDIS_KEY_PREFIX + "userScoreAverageList";

    // ==================== 视频详情缓存（Cache-Aside + 缓存三件套）====================
    /** 视频详情缓存：PiliPili:video:info:{videoId} */
    public static final String REDIS_KEY_VIDEO_INFO = REDIS_KEY_PREFIX + "video:info:";
    /** 视频详情缓存重建锁：PiliPili:video:info:lock:{videoId} */
    public static final String REDIS_KEY_VIDEO_INFO_LOCK = REDIS_KEY_PREFIX + "video:info:lock:";
    /** 缓存基础过期时间(秒)：30 分钟 */
    public static final int VIDEO_INFO_CACHE_BASE_SECONDS = 30 * 60;
    /** 缓存 TTL 随机抖动范围(秒)：0~5 分钟（防雪崩：避免批量 key 同时过期） */
    public static final int VIDEO_INFO_CACHE_RANDOM_SECONDS = 5 * 60;
    /** 空值缓存过期时间(秒)：2 分钟（防穿透：把"不存在"也缓存起来） */
    public static final int VIDEO_INFO_NULL_CACHE_SECONDS = 2 * 60;
    /** 重建锁自动过期时间(秒)：10 秒（防死锁：持锁线程挂掉后自动释放） */
    public static final int VIDEO_INFO_LOCK_SECONDS = 10;

    // ==================== 弹幕缓存（按时间分片 + 索引/内容分离）====================
    /**
     * 弹幕分片索引：PiliPili:danmu:idx:{fileId}:{slice}
     * ZSet：score=弹幕出现时间(秒)，member=barrageId
     * 为什么"索引"和"内容"要分开？
     * 单 key 存整条视频的弹幕会成为 big key（10 万条 ≈ 20MB，阻塞、删除慢、分片不均）；
     * 拆成"轻量索引 + 分片内容"后，既能按时间范围查询/按 id 精确删除，单 key 又只有几十 KB。
     */
    public static final String REDIS_KEY_DANMU_INDEX = REDIS_KEY_PREFIX + "danmu:idx:";
    /** 弹幕分片内容：PiliPili:danmu:data:{fileId}:{slice}，Hash：field=barrageId，value=弹幕 JSON */
    public static final String REDIS_KEY_DANMU_DATA = REDIS_KEY_PREFIX + "danmu:data:";
    /** 空分片占位：PiliPili:danmu:empty:{fileId}:{slice}（防穿透：确实没有弹幕的分片也标记一下） */
    public static final String REDIS_KEY_DANMU_EMPTY = REDIS_KEY_PREFIX + "danmu:empty:";
    /** 分片回源锁：PiliPili:danmu:lock:{fileId}:{slice}（防击穿：同一分片只放一个线程回源） */
    public static final String REDIS_KEY_DANMU_LOCK = REDIS_KEY_PREFIX + "danmu:lock:";
    /**
     * 弹幕最大时间缓存：PiliPili:danmu:maxtime:{fileId}
     * 用途：全量加载时要知道"分片扫描到哪一片为止"，否则每次请求都要 select max(time)。
     * 存 -1 表示"该分片确认没有弹幕"（防穿透）。
     */
    public static final String REDIS_KEY_DANMU_MAX_TIME = REDIS_KEY_PREFIX + "danmu:maxtime:";
    /** 分片粒度(秒)：5 分钟/片。片越小回源越细但 key 越多；按 1 条/秒的密度，单片≈300 条≈60KB */
    public static final int DANMU_SLICE_SECONDS = 5 * 60;
    /** 分片缓存基础过期时间(秒)：24 小时 */
    public static final int DANMU_CACHE_BASE_SECONDS = 24 * 60 * 60;
    /** 分片缓存 TTL 随机抖动(秒)：0~10 分钟（防雪崩：避免同一视频的所有分片同时过期） */
    public static final int DANMU_CACHE_RANDOM_SECONDS = 10 * 60;
    /** 空分片占位过期时间(秒)：60 秒（防穿透，注意要短，否则新弹幕迟迟不可见） */
    public static final int DANMU_EMPTY_CACHE_SECONDS = 60;
    /** 分片回源锁自动过期(秒)：10 秒 */
    public static final int DANMU_LOCK_SECONDS = 10;
    /** 单次最多加载的分片数（保护：避免超长视频/异常 duration 导致一次读几十个分片） */
    public static final int DANMU_MAX_SLICE_LOAD = 60;
    /** 增量拉取单次默认返回上限 */
    public static final int DANMU_INCREMENT_DEFAULT_LIMIT = 200;

}
