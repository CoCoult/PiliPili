package com.PiliPili.service;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.query.VideoPlayHistoryQuery;
import org.springframework.stereotype.Service;

@Service
public interface VideoPlayHistoryService {

    /**
     * 保存用户视频观看历史
     * @param userId
     * @param videoId
     * @param fileIndex
     */
    void SaveVideoHistory(String userId, String videoId, Integer fileIndex);

    /**
     * 获取当前用户的视频播放记录
     * @param videoPlayHistoryQuery
     * @return
     */
    PageBean GetVideoPlayHistory(VideoPlayHistoryQuery videoPlayHistoryQuery);

    /**
     * 清除用户所有观看历史记录
     * @param videoPlayHistoryQuery
     */
    void DeleteAllUserVideoPlayHistory(VideoPlayHistoryQuery videoPlayHistoryQuery);

    /**
     * 删除用户视频观看历史记录
     * @param videoPlayHistoryQuery
     */
    void DeleteUserVideoPlayHistoryByVideoId(VideoPlayHistoryQuery videoPlayHistoryQuery);
}
