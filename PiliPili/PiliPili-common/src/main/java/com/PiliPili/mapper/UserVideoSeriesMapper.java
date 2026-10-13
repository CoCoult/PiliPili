package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.UserVideoSeries;
import com.PiliPili.entity.query.UserVideoSeriesQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserVideoSeriesMapper {
    /**
     * 获取全部视频合集
     * @param userId
     * @return
     */
    List<UserVideoSeries> GetUserSeriesInfoList(String userId);

    /**
     * 新增视频合集
     */
    void InsertVideoSeries(UserVideoSeries userVideoSeries);

    /**
     * 获取最大合集序号
     * @param userId
     * @return
     */
    Integer GetMaxSeriesSort(String userId);

    /**
     * 根据id获取合集信息
     * @param seriesId
     * @return
     */
    UserVideoSeries GetUserSeriesBySeriesId(Integer seriesId);

    /**
     * 更新用户视频合集信息
     * @param userVideoSeriesQuery
     */
    void UpdateUserVideoSeries(UserVideoSeriesQuery userVideoSeriesQuery);

    /**
     * 删除视频合集
     *
     * @param userVideoSeriesQuery
     * @return
     */
    Integer DeleteUserVideoSeriesBySeriesId(UserVideoSeriesQuery userVideoSeriesQuery);

    /**
     * 获取视频合集以及合集前五个视频
     */
    List<UserVideoSeries> GetVideoSeriesListWithVideo(UserVideoSeriesQuery userVideoSeriesQuery);

    /**
     * 更新合集顺序
     * @param userVideoSeriesList
     */
    void UpdateVideoSeriesSort(@Param("userVideoSeriesList") List<UserVideoSeries> userVideoSeriesList);

}
