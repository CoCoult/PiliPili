package com.PiliPili.component;

import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.SysSettingDto;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.dto.UploadingFileDto;
import com.PiliPili.entity.dto.VideoPlayInfoDto;
import com.PiliPili.entity.pojo.Category;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.redis.RedisUtils;
import com.PiliPili.utils.StringToolsUtils;
import org.apache.http.client.utils.DateUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.PushbackInputStream;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class RedisComponent {
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

}
