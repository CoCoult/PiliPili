package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.SeriesVideo;
import com.PiliPili.entity.query.SeriesVideoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeriesVideoMapper {

    /**
     * 获取最大序号
     * @param seriesId
     * @return
     */
    Integer GetMaxSortBySeriesId(Integer seriesId);

    /**
     * 批量插入合集的视频信息
     * @param seriesVideoList
     */
    void InsertBatch(@Param("seriesVideoList") List<SeriesVideo> seriesVideoList);

    /**
     * 获取合集中的视频列表
     * @param seriesVideoQuery
     * @return
     */
    List<SeriesVideo> GetVideoListBySeriesId(SeriesVideoQuery seriesVideoQuery);

    /**
     * 根据videoId删除合集中的视频
     * @param seriesVideoQuery
     */
    void DeleteVideoBySeriesId(SeriesVideoQuery seriesVideoQuery);

    /**
     * 批量删除
     * @param seriesVideoList
     */
    void UpdateBatch(@Param("seriesVideoList") List<SeriesVideo> seriesVideoList);

}
