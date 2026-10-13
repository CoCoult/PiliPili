package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.StatisticsInfo;
import com.PiliPili.entity.pojo.VideoComment;
import com.PiliPili.entity.query.StatisticInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsInfoMapper {

    /**
     * 获取粉丝数据
     * @param StatisticData
     * @return
     */
    List<StatisticsInfo> SelectStatisticFans(@Param("StatisticData") String StatisticData);

    /**
     * 获取评论数据
     * @param StatisticData
     * @return
     */
    List<StatisticsInfo> SelectStatisticComment(@Param("StatisticData") String StatisticData);

    /**
     * 获取用户操作数据
     * @param StatisticData
     * @return
     */
    List<StatisticsInfo> SelectStatisticAction(@Param("StatisticData") String StatisticData, @Param("actionTypeList") Integer[] actionTypeList);

    /**
     * 批量插入数据
     * @param statisticsInfoList
     */
    void InsertBatch(@Param("statisticsInfoList") List<StatisticsInfo> statisticsInfoList);

    /**
     * 获取用户数据信息
     * @param query
     * @return
     */
    List<StatisticsInfo> GetStatisticInfoByParam(@Param("query")StatisticInfoQuery query);

    /**
     * 获取用户全部数据
     * @param userId
     * @return
     */
    Map<String, Integer> GetAllStatisticByUserId(@Param("userId") String userId);

    /**
     * 管理端获取前一天数据
     * @param query
     * @return
     */
    List<StatisticsInfo> SelectStatisticInfoByParamAdmin(@Param("query")StatisticInfoQuery query);

    /**
     * 获取前一周内新增用户数
     * @param query
     * @return
     */
    List<StatisticsInfo> SelectUserWeekNewCount(@Param("query")StatisticInfoQuery query);

    /**
     * 统计弹幕
     */
    StatisticsInfo SelectBarrage(@Param("StatisticData") String StatisticData, @Param("actionTypeList") Integer[] actionTypeList);
}
