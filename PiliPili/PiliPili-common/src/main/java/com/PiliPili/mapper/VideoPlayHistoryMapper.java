package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.VideoPlayHistory;
import com.PiliPili.entity.query.VideoPlayHistoryQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoPlayHistoryMapper {

    /**
     * 新增历史记录，若已存在则更新历史记录
     * @param videoPlayHistory
     */
    void SaveOrUpdateVideoHistory(VideoPlayHistory videoPlayHistory);

    /**
     * 获取用户视频播放历史记录
     * @param videoPlayHistoryQuery
     * @return
     */
    List<VideoPlayHistory> GetVideoPlayHistory(VideoPlayHistoryQuery videoPlayHistoryQuery);

    /**
     * 删除用户所有观看记录
     * @param videoPlayHistoryQuery
     */
    void DeleteUserVideoPlayHistory(VideoPlayHistoryQuery videoPlayHistoryQuery);

    /**
     * 获取用户的视频历史记录
     * @param userId
     * @return
     */
    List<String> GetVideoPlayHistoryVideoIdByUserId(@Param("userId") String userId);

    /**
     * 获取用户指定浏览记录
     * @param userId
     * @param videoId
     * @return
     */
    Integer GetVideoInfoByUserIdVideoId(@Param("userId")String userId, @Param("videoId") String videoId);
}
