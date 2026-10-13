package com.PiliPili.service;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.VideoInfoQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VideoInfoService {

    /**
     * 获取推荐视频
     */
    List<VideoInfo> GetRecommendVideo(VideoInfoQuery videoInfoQuery);

    /**
     * 获取分类视频
     */
    PageBean GetVideo(VideoInfoQuery videoInfoQuery);

    /**
     * 根据视频id获取视频信息
     */
    VideoInfo GetVideoInfoByVideoId(String videoId);

    /**
     * 获取视频列表
     * @param videoInfoQuery
     * @return
     */
    PageBean GetVideoList(VideoInfoQuery videoInfoQuery);

    /**
     * 获取该用户下的过滤过的视频
     * @param videoInfoQuery
     * @return
     */
    List<VideoInfo> GetVideoListByUserId(VideoInfoQuery videoInfoQuery);

    /**
     * 获取分类下的视频数
     * @param videoInfoQuery
     * @return
     */
    Integer GetVideoCount(VideoInfoQuery videoInfoQuery);

    /**
     * 是否开启弹幕和评论
     * @param videoId
     * @param userId
     * @param interaction
     */
    void ChangeInteraction(String videoId, String userId, String interaction);

    /**
     * 删除视频
     */
    void DeleteVideo(String videoId, String userId);

    /**
     * 增加视频播放量
     * @param videoId
     */
    void addVideoPlayCount(String videoId);
}

