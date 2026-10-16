package com.PiliPili.component;

import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.SysSettingDto;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.dto.UploadingFileDto;
import com.PiliPili.entity.dto.VideoPlayInfoDto;
import com.PiliPili.entity.pojo.Category;
import com.PiliPili.entity.pojo.VideoBarrage;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.redis.RedisUtils;
import com.PiliPili.utils.StringToolsUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.PushbackInputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RedisComponent {
    private static final Logger log = LoggerFactory.getLogger(RedisComponent.class);

    @Resource
    private RedisUtils redisUtils;

    @Resource
    AppConfig appConfig;

    /**
     * 保存验证码
     *
     * @param checkCode
     * @return
     */
    public String saveCheckCode(String checkCode) {
        //生成随机的验证码id以对应每个用户
        String checkCodeKey = UUID.randomUUID().toString();
        //将验证码结果存入redis
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, checkCode, Constants.REDIS_KEY_EXPIRE_TIME * 10);
        return checkCodeKey;
    }

    /**
     * 获取验证码的key
     */
    public String GetCheckCodeKey(String checkCodeKey) {
        return redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey).toString();
    }

    /**
     * 删除验证码
     */
    public void DeleteCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    /**
     * 保存用户信息至redis
     */
    public void SaveTokenUserInfo(TokenUserInfoDto tokenUserInfoDto) {
        String token = UUID.randomUUID().toString();
        tokenUserInfoDto.setExpireTime(System.currentTimeMillis() + Constants.REDIS_KEY_EXPIRE_TIME * 60 * 24 * 7);
        tokenUserInfoDto.setToken(token);
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRE_TIME * 60 * 24 * 7);
    }

    /**
     * 更新用户信息至redis
     */
    public void UpdateTokenUserInfo(TokenUserInfoDto tokenUserInfoDto) {
        String token = tokenUserInfoDto.getToken();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRE_TIME * 60 * 24 * 7);
    }

    /**
     * 根据token获取用户信息
     */
    public TokenUserInfoDto GetUserInfoByToken(String token) {
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    /**
     * 根据token获取管理员信息
     */
    public String GetAdminInfoByToken(String token) {
        return (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    /**
     * 从redis中删除用户token信息
     */
    public void DeleteUserInfo(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    /**
     * 从redis中删除管理员token信息
     */
    public void DeleteAdminInfo(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    /**
     * 存入管理员账号
     */
    public String SaveTokenAdmin(String account) {
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN + token, account, Constants.REDIS_KEY_EXPIRE_TIME * 60 * 24);
        return token;
    }

    /**
     * 将分类信息存入redis
     */
    public void SaveCategory2Redis(List<Category> categoryList) {
        redisUtils.set(Constants.REDIS_KEY_CATEGORY_LIST, categoryList);
    }

    /**
     * 从redis获取分类
     */
    public List<Category> GetCategoryFromRedis() {
        return (List<Category>) redisUtils.get(Constants.REDIS_KEY_CATEGORY_LIST);
    }

    /**
     * 存储视频文件信息
     */
    public String SaveVideoFileInfo(String fileName, Integer chunks, String userId) {
        String uploadId = StringToolsUtils.GetRandomString(Constants.LENGTH_10);
        UploadingFileDto fIleDto = new UploadingFileDto();
        fIleDto.setFileName(fileName);
        fIleDto.setChunks(chunks);
        fIleDto.setUploadId(uploadId);
        fIleDto.setChunkIndex(0);
        String day = DateUtils.formatDate(new Date(), Constants.DATE_FORMAT_2);
        String filePath = day + "/" + userId + "-" + uploadId;
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_TEMP + filePath;
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        fIleDto.setFilePath(filePath);
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId, fIleDto, Constants.REDIS_KEY_EXPIRE_TIME * 60 * 24);
        return uploadId;
    }

    /**
     * 获取视频信息
     */
    public UploadingFileDto GetVideoFileInfo(String userId, String uploadId) {
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    /**
     * 获取系统设置信息
     */
    public SysSettingDto GetSysSettingInfo() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        //若为空则设置默认值
        if (sysSettingDto == null) {
            sysSettingDto = new SysSettingDto();
        }
        return sysSettingDto;
    }

    /**
     * 保存系统设置
     * @param sysSettingDto
     */
    public void SaveSysSetting(SysSettingDto sysSettingDto){
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    /**
     * 更新上传的文件信息
     */
    public void UpdateUploadingFileInfo(UploadingFileDto uploadingFileDto, String userId) {
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadingFileDto.getUploadId(), uploadingFileDto, Constants.REDIS_KEY_EXPIRE_TIME * 60 * 24);
    }

    /**
     * 删除视频文件信息
     */
    public void DeleteVideoInfo(String userId, String uploadId) {
        redisUtils.delete(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    /**
     * 将文件路径放入redis消息队列
     */
    public void SaveDelFilePath2Queue(String videoId, List<String> delFilePath) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_DEL + videoId, delFilePath, Constants.REDIS_KEY_EXPIRE_TIME * 24 * 60 * 7);
    }

    /**
     * 取视频文件路径信息
     */
    public List<String> getDelFileList(String videoId) {
        return redisUtils.getQueueList(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    /**
     * 删除文件路径信息
     */
    public void CleanDelFileList(String videoId) {
        redisUtils.delete(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    /**
     * 将要新增保存的视频分P放入消息队列
     */
    public void SaveInsertVideoFilePost2Queue(List<VideoInfoFilePost> insertList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_INSERT, insertList, 0);
    }


    /**
     * 从消息队列取转码视频信息
     *
     * @return
     */
    public VideoInfoFilePost GetFileFromPostQueue() {
        return (VideoInfoFilePost) redisUtils.rpop(Constants.REDIS_KEY_FILE_INSERT);
    }

    /**
     * 从消息队列取视播放历史记录
     *
     * @return
     */
    public VideoPlayInfoDto GetVideoPlayFromQueue() {
        return (VideoPlayInfoDto) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY);
    }

    /**
     * 记录视频在线人数
     *
     * @param fileId
     * @param deviceId 浏览器标识
     */
    public Integer ReportVideoPlayOnline(String fileId, String deviceId) {
        //用户在线
        String userPlayOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USE, fileId, deviceId);
        //视频在线
        String videoPlayOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId);
        if (!redisUtils.keyExists(userPlayOnlineKey)) {
            redisUtils.setex(userPlayOnlineKey, fileId, Constants.REDIS_KEY_ONE_SECONDS * 8);
            return redisUtils.incrementex(videoPlayOnlineKey, Constants.REDIS_KEY_ONE_SECONDS * 10).intValue();
        }
        //有用户在观看则续期视频
        redisUtils.expire(userPlayOnlineKey, Constants.REDIS_KEY_ONE_SECONDS * 8);
        redisUtils.expire(videoPlayOnlineKey, Constants.REDIS_KEY_ONE_SECONDS * 10);
        Integer count = (Integer) redisUtils.get(videoPlayOnlineKey);
        return count == null ? 1 : count;
    }

    /**
     * 减少视频在线用户数
     *
     * @param key
     */
    public void DecreaseVideoPlayOnlineKey(String key) {
        redisUtils.decrement(key);
    }

    /**
     * 增加视频的搜索次数
     */
    public void addVideoSearchCount(String keyword) {
        redisUtils.zaddCount(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, keyword);
    }

    /**
     * 获取热度前top个的视频关键词
     *
     * @param top
     * @return
     */
    public List<String> GetKeywordPop(Integer top) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, top - 1);
    }

    public void addVideoPlay(VideoPlayInfoDto videoPlayInfoDto) {
        redisUtils.lpush(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY, videoPlayInfoDto, null);
    }

    /**
     * 记录每天的播放数
     */
    public void recordVideoPlayCount(String videoId) {
        String date = com.PiliPili.utils.DateUtils.format(new Date(), "yyyy-MM-dd");
        redisUtils.incrementex(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date + ":" + videoId, Constants.REDIS_KEY_ONE_SECONDS * 3600 * 24 * 2);
    }

    /**
     * 从redis获取播放量
     * @param date
     * @return
     */
    public Map<String, Integer> GetVideoPlayCountByVideoQueue(String date) {
        Map<String, Integer> map = redisUtils.getBatch(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date);
        return map;
    }

    /**
     * 保存用户评分矩阵
     * @param userScoreMatrix
     */
    public void SavaUserScoreMatrix(Map<String, Map<String, Double>> userScoreMatrix) {
        redisUtils.set(Constants.User_SCORE_MATRIX, userScoreMatrix);
    }

    /**
     * 获取用户评分矩阵
     * @return
     */
    public Map<String, Map<String, Double>> GetUserScoreMatrix() {
        return (Map<String, Map<String, Double>>) redisUtils.get(Constants.User_SCORE_MATRIX);
    }

    /**
     * 保存
     * @param averageScoreList
     */
    public void SavaUserScoreAverageList(Map<String, Double> averageScoreList) {
        redisUtils.set(Constants.User_SCORE_AVERAGE, averageScoreList);
    }

    /**
     * 获取评分平均值
     * @return
     */
    public Map<String, Double> GetUserScoreAverageList() {
        return (Map<String, Double>) redisUtils.get(Constants.User_SCORE_AVERAGE);
    }

    // ==================== 弹幕分片缓存 ====================
    //
    // 存储结构（索引与内容分离，避免 big key）：
    //   ZSet  PiliPili:danmu:idx:{fileId}:{slice}   score=出现时间(秒)  member=barrageId   —— 轻量索引，支持按时间范围查
    //   Hash  PiliPili:danmu:data:{fileId}:{slice}  field=barrageId     value=弹幕JSON     —— 分片内容，支持按 id 精确删除
    //   String PiliPili:danmu:empty:{fileId}:{slice}  空分片占位（防穿透）
    //   String PiliPili:danmu:lock:{fileId}:{slice}   分片回源锁（防击穿）

    /**
     * 计算某个时间点落在哪个分片
     */
    public static int calcDanmuSlice(Integer time) {
        if (time == null || time < 0) {
            return 0;
        }
        return time / Constants.DANMU_SLICE_SECONDS;
    }

    /**
     * 分片起始秒（含）
     */
    public static int danmuSliceStart(int slice) {
        return slice * Constants.DANMU_SLICE_SECONDS;
    }

    /**
     * 分片结束秒（不含）
     */
    public static int danmuSliceEnd(int slice) {
        return (slice + 1) * Constants.DANMU_SLICE_SECONDS;
    }

    public static String danmuIndexKey(String fileId, int slice) {
        return Constants.REDIS_KEY_DANMU_INDEX + fileId + ":" + slice;
    }

    public static String danmuDataKey(String fileId, int slice) {
        return Constants.REDIS_KEY_DANMU_DATA + fileId + ":" + slice;
    }

    public static String danmuEmptyKey(String fileId, int slice) {
        return Constants.REDIS_KEY_DANMU_EMPTY + fileId + ":" + slice;
    }

    public static String danmuLockKey(String fileId, int slice) {
        return Constants.REDIS_KEY_DANMU_LOCK + fileId + ":" + slice;
    }

    /**
     * 尝试读取一个分片
     *
     * @return <b>null</b> 表示"这个分片还没缓存，需要回源 DB"；
     * <b>空集合</b> 表示"已确认这个分片确实没有弹幕"（命中空占位，防穿透）；
     * 有数据则返回按时间升序的弹幕列表
     */
    @SuppressWarnings("unchecked")
    public List<VideoBarrage> tryGetDanmuSlice(String fileId, int slice) {
        try {
            // 空分片占位命中 → 直接返回空，不打库
            if (redisUtils.keyExists(danmuEmptyKey(fileId, slice))) {
                return new ArrayList<>();
            }
            String indexKey = danmuIndexKey(fileId, slice);
            if (!redisUtils.keyExists(indexKey)) {
                return null;
            }
            int start = danmuSliceStart(slice);
            int end = danmuSliceEnd(slice) - 1;
            Set<Object> idSet = redisUtils.zrangeByScore(indexKey, start, end);
            if (idSet == null || idSet.isEmpty()) {
                return new ArrayList<>();
            }
            List<String> ids = idSet.stream().map(String::valueOf).collect(Collectors.toList());
            List<Object> values = redisUtils.hmget(danmuDataKey(fileId, slice), ids);
            List<VideoBarrage> result = new ArrayList<>();
            for (Object value : values) {
                if (value instanceof VideoBarrage) {
                    result.add((VideoBarrage) value);
                }
            }
            sortDanmu(result);
            return result;
        } catch (Exception e) {
            // Redis 异常时返回 null → 调用方降级查库，保证弹幕功能可用
            log.warn("读取弹幕分片缓存失败,fileId:{},slice:{}", fileId, slice, e);
            return null;
        }
    }

    /**
     * 回填一个分片（TTL 24h + 随机抖动防雪崩）
     * <p>
     * 列表为空时写"空分片占位"（短 TTL），防止没有弹幕的分片被反复回源
     */
    public void saveDanmuSlice(String fileId, int slice, List<VideoBarrage> list) {
        try {
            long ttl = randomDanmuSeconds(Constants.DANMU_CACHE_BASE_SECONDS, Constants.DANMU_CACHE_RANDOM_SECONDS);
            if (list == null || list.isEmpty()) {
                redisUtils.setex(danmuEmptyKey(fileId, slice), (Object) "1", Constants.DANMU_EMPTY_CACHE_SECONDS * 1000L);
                return;
            }
            Map<String, Object> dataMap = new HashMap<>();
            for (VideoBarrage barrage : list) {
                String id = String.valueOf(barrage.getBarrageId());
                redisUtils.zadd(danmuIndexKey(fileId, slice), (Object) id, barrage.getTime() == null ? 0 : barrage.getTime());
                dataMap.put(id, barrage);
            }
            redisUtils.hmset(danmuDataKey(fileId, slice), (Map) dataMap, ttl * 1000L);
            redisUtils.expire(danmuIndexKey(fileId, slice), ttl * 1000L);
            redisUtils.delete(danmuEmptyKey(fileId, slice));
        } catch (Exception e) {
            log.warn("回填弹幕分片缓存失败,fileId:{},slice:{}", fileId, slice, e);
        }
    }

    /**
     * 失效一个分片（新增/删除弹幕后调用，下次读请求自然回源重建）
     */
    public void deleteDanmuSlice(String fileId, int slice) {
        try {
            redisUtils.delete(danmuIndexKey(fileId, slice), danmuDataKey(fileId, slice), danmuEmptyKey(fileId, slice));
        } catch (Exception e) {
            log.warn("删除弹幕分片缓存失败,fileId:{},slice:{}", fileId, slice, e);
        }
    }

    /**
     * 增量拉取：取 (fromTime,fromId] ~ toTime 之间新增的弹幕
     * <p>
     * 游标为什么必须是 (时间, ID) 二元组？弹幕的 time 是"秒"，同一秒可能有很多条，
     * 只用时间做游标会漏取（同一秒的后续弹幕）或重复取。
     */
    @SuppressWarnings("unchecked")
    public List<VideoBarrage> getDanmuIncrement(String fileId, int fromTime, int fromId, int toTime, int limit) {
        List<VideoBarrage> result = new ArrayList<>();
        if (toTime < fromTime) {
            return result;
        }
        int fromSlice = calcDanmuSlice(fromTime);
        int toSlice = calcDanmuSlice(toTime);
        for (int slice = fromSlice; slice <= toSlice && result.size() < limit; slice++) {
            String indexKey = danmuIndexKey(fileId, slice);
            if (!redisUtils.keyExists(indexKey)) {
                // 索引不在缓存里：交给上层按分片回源（这里只负责"已缓存部分"的增量）
                continue;
            }
            int start = Math.max(fromTime, danmuSliceStart(slice));
            int end = Math.min(toTime, danmuSliceEnd(slice) - 1);
            Set<Object> idSet = redisUtils.zrangeByScore(indexKey, start, end, 0, limit);
            if (idSet == null || idSet.isEmpty()) {
                continue;
            }
            List<String> ids = idSet.stream().map(String::valueOf).collect(Collectors.toList());
            List<Object> values = redisUtils.hmget(danmuDataKey(fileId, slice), ids);
            for (Object value : values) {
                if (!(value instanceof VideoBarrage)) {
                    continue;
                }
                VideoBarrage barrage = (VideoBarrage) value;
                // 同秒排重：只取"时间更大"或"时间相同但 ID 更大"的
                if (barrage.getTime() != null
                        && (barrage.getTime() > fromTime
                        || (barrage.getTime() == fromTime && barrage.getBarrageId() != null && barrage.getBarrageId() > fromId))) {
                    result.add(barrage);
                }
            }
        }
        sortDanmu(result);
        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    /**
     * 读取"该 fileId 弹幕的最大出现时间"缓存
     *
     * @return null=未缓存（需要查库）；-1=确认没有弹幕；>=0=最大时间(秒)
     */
    public Integer getDanmuMaxTime(String fileId) {
        try {
            Object value = redisUtils.get(Constants.REDIS_KEY_DANMU_MAX_TIME + fileId);
            return value == null ? null : Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            log.warn("读取弹幕最大时间缓存失败,fileId:{}", fileId, e);
            return null;
        }
    }

    public void saveDanmuMaxTime(String fileId, int maxTime) {
        try {
            long ttl = randomDanmuSeconds(Constants.DANMU_CACHE_BASE_SECONDS, Constants.DANMU_CACHE_RANDOM_SECONDS);
            redisUtils.setex(Constants.REDIS_KEY_DANMU_MAX_TIME + fileId, (Object) String.valueOf(maxTime), ttl * 1000L);
        } catch (Exception e) {
            log.warn("写入弹幕最大时间缓存失败,fileId:{}", fileId, e);
        }
    }

    /**
     * 失效"最大时间"缓存（有新弹幕写入时，最大值可能变大）
     */
    public void deleteDanmuMaxTime(String fileId) {
        try {
            redisUtils.delete(Constants.REDIS_KEY_DANMU_MAX_TIME + fileId);
        } catch (Exception e) {
            log.warn("删除弹幕最大时间缓存失败,fileId:{}", fileId, e);
        }
    }

    /**
     * 清空某个 fileId 的全部弹幕缓存（视频被删除时调用）
     * <p>
     * 注意：这里用 keys 前缀匹配（O(N)），只用在"删除视频"这种低频管理操作上，
     * 高频路径绝不能用；生产环境应换成 SCAN 游标遍历。
     */
    public void deleteDanmuCacheAllSlices(String fileId) {
        try {
            for (String prefix : new String[]{Constants.REDIS_KEY_DANMU_INDEX,
                    Constants.REDIS_KEY_DANMU_DATA,
                    Constants.REDIS_KEY_DANMU_EMPTY,
                    Constants.REDIS_KEY_DANMU_LOCK}) {
                Set<String> keys = redisUtils.getByKeyPrefix(prefix + fileId + ":");
                if (keys != null && !keys.isEmpty()) {
                    redisUtils.delete(keys.toArray(new String[0]));
                }
            }
            redisUtils.delete(Constants.REDIS_KEY_DANMU_MAX_TIME + fileId);
        } catch (Exception e) {
            log.warn("清空弹幕缓存失败,fileId:{}", fileId, e);
        }
    }

    /**
     * 取分片回源锁（防击穿）
     *
     * @return 锁的持有凭证（释放时要带回来校验）；null 表示没抢到
     */    public String tryLockDanmuSlice(String fileId, int slice) {
        String lockKey = danmuLockKey(fileId, slice);
        String lockValue = UUID.randomUUID().toString();
        boolean locked = redisUtils.setnx(lockKey, (Object) lockValue, Constants.DANMU_LOCK_SECONDS * 1000L);
        return locked ? lockValue : null;
    }

    public void unlockDanmuSlice(String fileId, int slice, String lockValue) {
        redisUtils.releaseLock(danmuLockKey(fileId, slice), (Object) lockValue);
    }

    /**
     * 按 (时间, ID) 升序排序：同一秒内按 ID 升序，保证多次读取顺序稳定、增量不漏不重
     */
    public static void sortDanmu(List<VideoBarrage> list) {
        list.sort((a, b) -> {
            int timeA = a.getTime() == null ? 0 : a.getTime();
            int timeB = b.getTime() == null ? 0 : b.getTime();
            if (timeA != timeB) {
                return Integer.compare(timeA, timeB);
            }
            int idA = a.getBarrageId() == null ? 0 : a.getBarrageId();
            int idB = b.getBarrageId() == null ? 0 : b.getBarrageId();
            return Integer.compare(idA, idB);
        });
    }

    /**
     * TTL 随机抖动（防雪崩）
     */
    private long randomDanmuSeconds(int baseSeconds, int randomSeconds) {
        return baseSeconds + (long) (Math.random() * randomSeconds);
    }

}
