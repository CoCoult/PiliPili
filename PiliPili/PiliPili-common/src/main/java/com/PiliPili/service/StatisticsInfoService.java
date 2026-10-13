package com.PiliPili.service;

import com.PiliPili.entity.pojo.StatisticsInfo;
import com.PiliPili.entity.query.StatisticInfoQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface StatisticsInfoService {

    /**
     * 统计数据
     */
    void StatisticData();

    /**
     * 获取用户数据信息
     * @param statisticInfoQuery
     * @return
     */
    List<StatisticsInfo> GetStatisticInfoByParam(StatisticInfoQuery statisticInfoQuery);

    /**
     * 管理端获取前一天数据数据
     * @param statisticInfoQuery
     * @return
     */
    List<StatisticsInfo> GetStatisticInfoByParamAdmin(StatisticInfoQuery statisticInfoQuery);

    /**
     * 获取用户全部数据
     * @param userId
     * @return
     */
    Map<String, Integer> GetAllStatistic(String userId);

    /**
     * 获取一周内的新增用户
     * @param statisticInfoQuery
     * @return
     */
    List<StatisticsInfo> GetUserWeekNewCount(StatisticInfoQuery statisticInfoQuery);

    /**
     * 更新用户评分矩阵
     */
    void SaveUserScoreMatrix();
}
