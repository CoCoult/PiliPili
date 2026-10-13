package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.VideoInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoInfoMapper {

    /**
     * 获取推荐视频
     */
    List<VideoInfo> GetRecommendVideo(VideoInfoQuery videoInfoQuery);

    /**
     * 获取分类视频
     */
    List<VideoInfo> GetVideo(VideoInfoQuery videoInfoQuery);

    /**
     * 根据视频id获取视频信息
     *
     * @param videoId
     */
    VideoInfo GetVideoInfoByVideoId(String videoId);

    /**
     * 更新视频信息指定属性的数量
     * @param videoId
     * @param changeCount
     * @param field
     */
    void UpdateVideoCount(String videoId, Integer changeCount, String field);

    /**
     * 获取视频列表
     * @param videoInfoQuery
     * @return
     */
    List<VideoInfo> GetVideoList(VideoInfoQuery videoInfoQuery);

    /**
     * 获取视频条数
     * @param videoInfoQuery
     * @return
     */
    Integer GetVideoCount(VideoInfoQuery videoInfoQuery);

    /**
     * 获取分类下视频数量
     * @param videoInfoQuery
     * @return
     */
    Integer GetVideoCountByCategoryId(VideoInfoQuery videoInfoQuery);

    /**
     * 根据视频id查询视频
     * @param videoId
     * @return
     */
    VideoInfo SelectByVideoId(@Param("videoId") String videoId);

    /**
     * 插入视频
     * @param videoInfo
     */
    void insertOrUpdate(VideoInfo videoInfo);

    /**
     * 更新信息
     */
    void UpdateByParam(@Param("videoInfo") VideoInfo videoInfo, @Param("query") VideoInfoQuery query);

    /**
     * 删除视频
     * @param videoId
     */
    void DeleteByVideoId(String videoId);

    /**
     * 获取所有视频id
     */
    List<String> GetAllVideoId();

}
