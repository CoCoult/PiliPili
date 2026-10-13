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


}
