package com.PiliPili.service;

import com.PiliPili.entity.pojo.UserVideoSeries;
import com.PiliPili.entity.query.UserVideoSeriesQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserVideoSeriesService {
    /**
     * 获取视频合集信息
     * @param userId
     * @return
     */
    List<UserVideoSeries> GetSeriesInfoList(String userId);

    /**
     * 新增或修改视频合集
     * @param seriesId
     * @param userVideoSeries
     */
    void SaveUserVideoSeries(String seriesId, UserVideoSeries userVideoSeries);

    /**
     * 保存该合集的视频信息
     * @param userId
     * @param seriesId
     * @param videoIds
     */
    void SaveSeriesVideoInfo(String userId, Integer seriesId, String videoIds);

    /**
     * 更新该合集的视频信息
     * @param userId
     * @param seriesId
     * @param videoIds
     */
    void UpdateSeriesVideoInfo(String userId, Integer seriesId, String videoIds);

    /**
     * 根据合集id查询合集信息
     * @param seriesId
     * @return
     */
    UserVideoSeries GetVideoSeriesBySeriesId(Integer seriesId);

    /**
     * 删除集合中的视频
     *
     * @param seriesId
     * @param videoId
     * @param userId
     */
    void DeleteSeriesVideo(Integer seriesId, String videoId, String userId);

    /**
     * 删除视频集合
     * @param seriesId
     * @param userId
     */
    void DeleteVideoSeriesBySeriesId(Integer seriesId, String userId);

    /**
     * 获取视频合集以及合集的前五个视频
     * @param userVideoSeriesQuery
     * @return
     */
    List<UserVideoSeries> GetVideoSeriesListWithVideo(UserVideoSeriesQuery userVideoSeriesQuery);

    /**
     * 修改合集顺序
     * @param seriesIds
     * @param userId
     */
    void ChangeVideoSeriesSort(String seriesIds, String userId);
}
