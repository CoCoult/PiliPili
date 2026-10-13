package com.PiliPili.component;

import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.mapper.UserMapper;
import com.PiliPili.mapper.VideoPlayHistoryMapper;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecommendComponent {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoPlayHistoryMapper videoPlayHistoryMapper;

    @Resource
    private UserMapper userMapper;


    /**
     * 计算目标用户皮尔逊相似度
     *
     * @param targetUserId
     * @return
     */
    public Map<String, Double> ComputePearsonSimilarity(String targetUserId) {
        Map<String, Map<String, Double>> userScoreMap = redisComponent.GetUserScoreMatrix();
        Map<String, Double> averageList = redisComponent.GetUserScoreAverageList();
        Map<String, Double> pearsonSimilarity = new HashMap<>();
        Map<String, Double> targetUserScore = userScoreMap.get(targetUserId);
        //过滤出用户之间都观看过的视频
        Map<String, Map<String, Double>> filterUserScoreMap = new HashMap<>();
        userScoreMap.forEach((userId, videoScoreMap) -> {
            if (!userId.equals(targetUserId)) {
                Map<String, Double> filterVideoMap = new HashMap<>();
                videoScoreMap.forEach((videoId, score) -> {
                    if (score != -1 && targetUserScore.get(videoId) != -1) {
                        score = score - averageList.get(userId);
                        filterVideoMap.put(videoId, score);
                    }
                });
                filterUserScoreMap.put(userId, filterVideoMap);
            }
        });
        //计算相似度
        targetUserScore.replaceAll((videoId, score) ->
                score - averageList.get(targetUserId)
        );
        List<String> userIdList = this.userMapper.GetAllUserId();
        //计算目标用户和其它用户相似度的分子
        Map<String, Double> numerator = new HashMap<>();
        userIdList.forEach(userId -> {
            if (!targetUserId.equals(userId)) {
                Double[] num = {0.0};
                targetUserScore.forEach((videoId, score) -> {
                    num[0] = num[0] + score * filterUserScoreMap.get(userId).get(videoId);
                });
                numerator.put(userId, num[0]);
            }
        });
        //计算目标用户和其它用户相似度的分母
        Map<String, Double> deNumerator = new HashMap<>();
        userIdList.forEach(userId -> {
            if (!targetUserId.equals(userId)) {
                Double[] num1 = {0.0};
                Double[] num2 = {0.0};
                targetUserScore.forEach((videoId, score) -> {
                    num1[0] = num1[0] + score * score;
                });
                filterUserScoreMap.get(userId).forEach((videoId, score) -> {
                    num2[0] = num2[0] + score * score;
                });
                deNumerator.put(userId, (Math.sqrt(num1[0]) * Math.sqrt(num2[0])));
            }
        });
        //保存皮尔逊系数
        userIdList.forEach(userId -> {
            if (!targetUserId.equals(userId)) {
                pearsonSimilarity.put(userId, numerator.get(userId) / deNumerator.get(userId));
            }
        });
        //由于系统用户较少，故选择五个相似度最高的用户作为推荐
        List<String> top5Users = pearsonSimilarity.entrySet().stream()
                // 过滤负相似度用户（根据需求可选）
                .filter(entry -> entry.getValue() > 0)
                // 按相似度降序排序
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                // 限制最多取5个
                .limit(5)
                // 提取用户ID
                .map(Map.Entry::getKey)
                // 收集为List
                .collect(Collectors.toList());
        Map<String, Double> top5User = new HashMap<>();
        top5Users.forEach(userId -> {
            top5User.put(userId, pearsonSimilarity.get(userId));
        });
        return top5User;
    }

    public List<VideoInfo> RecommendVideo(String targetUserId){
        return null;
    }
}

