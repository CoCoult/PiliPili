package com.PiliPili.service.impl;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoPlayHistory;
import com.PiliPili.entity.query.VideoPlayHistoryQuery;
import com.PiliPili.mapper.VideoPlayHistoryMapper;
import com.PiliPili.service.VideoPlayHistoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VideoPlayHistoryServiceImpl implements VideoPlayHistoryService {

    @Resource
    VideoPlayHistoryMapper videoPlayHistoryMapper;

    /**
     * 保存用户视频观看历史
     *
     * @param userId
     * @param videoId
     * @param fileIndex
     */
    @Override
    public void SaveVideoHistory(String userId, String videoId, Integer fileIndex) {
        VideoPlayHistory videoPlayHistory = new VideoPlayHistory();
        videoPlayHistory.setVideoId(videoId);
        videoPlayHistory.setUserId(userId);
        videoPlayHistory.setFileIndex(fileIndex);
        videoPlayHistory.setLastUpdateTime(LocalDateTime.now());
        this.videoPlayHistoryMapper.SaveOrUpdateVideoHistory(videoPlayHistory);
    }

    /**
     * 获取当前用户的视频播放记录
     *
     * @param videoPlayHistoryQuery
     * @return
     */
    @Override
    public PageBean GetVideoPlayHistory(VideoPlayHistoryQuery videoPlayHistoryQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoPlayHistoryQuery.getPageNumber(), videoPlayHistoryQuery.getPageSize());
        List<VideoPlayHistory> videoPlayHistoryList = this.videoPlayHistoryMapper.GetVideoPlayHistory(videoPlayHistoryQuery);
        Page<VideoPlayHistory> videoPlayHistoryPage = (Page<VideoPlayHistory>) videoPlayHistoryList;
        pageBean.setRows(videoPlayHistoryPage.getResult());
        pageBean.setTotal(videoPlayHistoryPage.getTotal());
        pageBean.setPageTotal(videoPlayHistoryPage.getPages());
        pageBean.setPageSize(videoPlayHistoryQuery.getPageSize());
        pageBean.setPageNum(videoPlayHistoryQuery.getPageNumber());
        return pageBean;
    }

    /**
     * 清除用户所有观看历史记录
     *
     * @param videoPlayHistoryQuery
     */
    @Override
    public void DeleteAllUserVideoPlayHistory(VideoPlayHistoryQuery videoPlayHistoryQuery) {
        this.videoPlayHistoryMapper.DeleteUserVideoPlayHistory(videoPlayHistoryQuery);
    }

    /**
     * 删除用户视频观看历史记录
     *
     * @param videoPlayHistoryQuery
     */
    @Override
    public void DeleteUserVideoPlayHistoryByVideoId(VideoPlayHistoryQuery videoPlayHistoryQuery) {
        this.videoPlayHistoryMapper.DeleteUserVideoPlayHistory(videoPlayHistoryQuery);
    }
}
