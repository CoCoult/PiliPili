package com.PiliPili.service.impl;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.enums.StatisticsTypeEnum;
import com.PiliPili.entity.pojo.*;
import com.PiliPili.entity.query.StatisticInfoQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.entity.query.VideoPlayHistoryQuery;
import com.PiliPili.mapper.*;
import com.PiliPili.service.StatisticsInfoService;
import com.PiliPili.utils.DateUtils;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoMapper videoInfoMapper;

    @Resource
    private StatisticsInfoMapper statisticsInfoMapper;

    @Resource
    private UserFocusMapper userFocusMapper;

    @Resource
    private UserActionMapper userActionMapper;

    @Resource
    private VideoBarrageMapper videoBarrageMapper;

    @Resource
    private VideoPlayHistoryMapper videoPlayHistoryMapper;


    /**
     * 统计数据
     */
    @Override
    public void StatisticData() {
        List<StatisticsInfo> statisticsInfoList = new ArrayList<>();
        String staticsDate = DateUtils.GetBeforeDate(Constants.ONE);

        //统计播放量
        Map<String, Integer> videoPlayCountMap = redisComponent.GetVideoPlayCountByVideoQueue(staticsDate);

        List<String> videoPlayList = new ArrayList<>(videoPlayCountMap.keySet());
        videoPlayList = videoPlayList.stream().map(item -> item.substring(item.lastIndexOf(":") + 1)).collect(Collectors.toList());

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoIdArray(videoPlayList.toArray(new String[videoPlayList.size()]));
        List<VideoInfo> videoInfoList = this.videoInfoMapper.GetVideoList(videoInfoQuery);

        Map<String, Integer> videoCountMap = videoInfoList.stream().collect(Collectors.groupingBy(VideoInfo::getUserId, Collectors.summingInt(item -> videoPlayCountMap.get(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + staticsDate + ":" + item.getVideoId()))));

        videoCountMap.forEach((key, value) -> {
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setUserId(key);
            statisticsInfo.setStatisticsCount(value);
            statisticsInfo.setStatisticsDate(staticsDate);
            statisticsInfo.setDataType(StatisticsTypeEnum.PLAY.getType());
            statisticsInfoList.add(statisticsInfo);
        });

        //统计粉丝数
        List<StatisticsInfo> fansList = this.statisticsInfoMapper.SelectStatisticFans(staticsDate);
        for (StatisticsInfo statisticsInfo : fansList) {
            statisticsInfo.setDataType(StatisticsTypeEnum.USER.getType());
            statisticsInfo.setStatisticsDate(staticsDate);
        }
        statisticsInfoList.addAll(fansList);

        //统计评论数
        List<StatisticsInfo> commentList = this.statisticsInfoMapper.SelectStatisticComment(staticsDate);
        for (StatisticsInfo statisticsInfo : commentList) {
            statisticsInfo.setDataType(StatisticsTypeEnum.COMMENT.getType());
            statisticsInfo.setStatisticsDate(staticsDate);
        }
        statisticsInfoList.addAll(commentList);

        //点赞，投币，收藏数
        List<StatisticsInfo> actionList = this.statisticsInfoMapper.SelectStatisticAction(staticsDate, new Integer[]{
                ActionTypeEnum.LIKE_VIDEO.getType(), ActionTypeEnum.COIN_VIDEO.getType(), ActionTypeEnum.COLLECT_VIDEO.getType()
        });
        //统计弹幕
        StatisticsInfo barrageData = this.statisticsInfoMapper.SelectBarrage(staticsDate, new Integer[]{ActionTypeEnum.VIDEO_BARRAGE.getType()});
        barrageData.setStatisticsDate(staticsDate);
        for (StatisticsInfo statisticsInfo : actionList) {
            statisticsInfo.setStatisticsDate(staticsDate);
            if (statisticsInfo.getDataType().equals(ActionTypeEnum.VIDEO_BARRAGE.getType())) {
                statisticsInfo.setDataType(StatisticsTypeEnum.BARRAGE.getType());
            } else if (statisticsInfo.getDataType().equals(ActionTypeEnum.LIKE_VIDEO.getType())) {
                statisticsInfo.setDataType(StatisticsTypeEnum.LIKE.getType());
            } else if (statisticsInfo.getDataType().equals(ActionTypeEnum.COIN_VIDEO.getType())) {
                statisticsInfo.setDataType(StatisticsTypeEnum.COIN.getType());
            } else if (statisticsInfo.getDataType().equals(ActionTypeEnum.COLLECT_VIDEO.getType())) {
                statisticsInfo.setDataType(StatisticsTypeEnum.COLLECTION.getType());
            }
        }
        statisticsInfoList.add(barrageData);
        statisticsInfoList.addAll(actionList);
        this.statisticsInfoMapper.InsertBatch(statisticsInfoList);
    }

    /**
     * 获取用户数据信息
     *
     * @param statisticInfoQuery
     * @return
     */
    @Override
    public List<StatisticsInfo> GetStatisticInfoByParam(StatisticInfoQuery statisticInfoQuery) {
        return this.statisticsInfoMapper.GetStatisticInfoByParam(statisticInfoQuery);
    }

    /**
     * 管理端获取前一天数据数据
     *
     * @param statisticInfoQuery
     * @return
     */
    @Override
    public List<StatisticsInfo> GetStatisticInfoByParamAdmin(StatisticInfoQuery statisticInfoQuery) {
        return this.statisticsInfoMapper.SelectStatisticInfoByParamAdmin(statisticInfoQuery);
    }

    /**
     * 获取用户全部数据
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Integer> GetAllStatistic(String userId) {
        Map<String, Integer> dataMap = this.statisticsInfoMapper.GetAllStatisticByUserId(userId);
        if (userId != null) {
            dataMap.put("userCount", this.userFocusMapper.GetUserFansByUserId(userId));
        } else {
            dataMap.put("userCount", this.userMapper.GetAllUserCount());
        }
        return dataMap;
    }

    /**
     * 获取一周内的新增用户
     *
     * @param statisticInfoQuery
     * @return
     */
    @Override
    public List<StatisticsInfo> GetUserWeekNewCount(StatisticInfoQuery statisticInfoQuery) {
        return this.statisticsInfoMapper.SelectUserWeekNewCount(statisticInfoQuery);
    }

    /**
     * 更新用户评分矩阵
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void SaveUserScoreMatrix() {
        //构建评分矩阵
        Map<String, Map<String, Double>> userScoreMap = new HashMap<>();
        //计算用户对视频的评分
        //首先获取用户历史记录，若用户观看过视频则评分，若未观看过则评分设为已观看过视频评分的平均值
        List<String> userIdList = this.userMapper.GetAllUserId();
        List<String> videoIdList = this.videoInfoMapper.GetAllVideoId();
        List<UserAction> userActionList = null;
        String preDate = DateUtils.GetBeforeDate(Constants.ONE);
        //保存用户的视频平均分
        Map<String, Double> averageScoreMap = new HashMap<>();
        for (String userId : userIdList) {
            Map<String, Double> videoScoreMap = new HashMap<>();
            Double sum = 0.0;
            for (String videoId : videoIdList) {
                userActionList = this.userActionMapper.GetUserActionListByUserId(userId);
                Integer barrageCount = this.videoBarrageMapper.GetVideoBarrageCountByUserId(userId, videoId);
                double score = 0;
                //计算用户评分, 其中不同操纵占不同权重，本系统权重如下，每个操作的原始分为1分。
                // 投币：30%
                //收藏：25%
                //点赞：20%
                //评论：15%
                //弹幕：10%
                //同是考虑到时间因素的影响，在一天之内进行的操作的最终得分再乘以120%(弹幕除外）
                for (UserAction userAction : userActionList) {
                    if (userAction.getActionType().equals(ActionTypeEnum.LIKE_COMMENT.getType())) {
                        if (userAction.getActionTime().equals(preDate)) {
                            score += Constants.ONE * 0.15 * 1.2;
                        } else {
                            score += Constants.ONE * 0.15;
                        }
                    }
                    if (userAction.getActionType().equals(ActionTypeEnum.COIN_VIDEO.getType())) {
                        if (userAction.getActionTime().equals(preDate)) {
                            score += Constants.ONE * 0.3 * 1.2;
                        } else {
                            score += Constants.ONE * 0.3;
                        }
                    }
                    if (userAction.getActionType().equals(ActionTypeEnum.LIKE_VIDEO.getType())) {
                        if (userAction.getActionTime().equals(preDate)) {
                            score += Constants.ONE * 0.25 * 1.2;
                        } else {
                            score += Constants.ONE * 0.2;
                        }
                    }
                    if (userAction.getActionType().equals(ActionTypeEnum.COLLECT_VIDEO.getType())) {
                        if (userAction.getActionTime().equals(preDate)) {
                            score += Constants.ONE * 0.25 * 1.2;
                        } else {
                            score += Constants.ONE * 0.25;
                        }
                    }
                }
                //评分求和
                sum += score;
                score = score + barrageCount * 0.1;
                //处理观看过但未进行任何互动,评分默认为0，若未观看视频则设为-1作为标记
                Integer isWatchVideo = this.videoPlayHistoryMapper.GetVideoInfoByUserIdVideoId(userId, videoId);
                if(isWatchVideo == 0) {
                    score = -1;
                }
                videoScoreMap.put(videoId, score);
            }
            averageScoreMap.put(userId , sum / videoIdList.size());
            userScoreMap.put(userId, videoScoreMap);
        }
        redisComponent.SavaUserScoreMatrix(userScoreMap);
        redisComponent.SavaUserScoreAverageList(averageScoreMap);
    }

}
