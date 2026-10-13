package com.PiliPili.service.impl;

import com.PiliPili.entity.pojo.SeriesVideo;
import com.PiliPili.entity.pojo.UserVideoSeries;
import com.PiliPili.entity.query.SeriesVideoQuery;
import com.PiliPili.entity.query.UserVideoSeriesQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.SeriesVideoMapper;
import com.PiliPili.mapper.UserVideoSeriesMapper;
import com.PiliPili.mapper.VideoInfoMapper;
import com.PiliPili.service.UserVideoSeriesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserVideoSeriesServiceImpl implements UserVideoSeriesService {

    @Resource
    UserVideoSeriesMapper userVideoSeriesMapper;

    @Resource
    VideoInfoMapper videoInfoMapper;

    @Resource
    SeriesVideoMapper seriesVideoMapper;

    /**
     * 获取视频合集信息

     * @param userId
     * @return
     */
    @Override
    public List<UserVideoSeries> GetSeriesInfoList(String userId) {
        return this.userVideoSeriesMapper.GetUserSeriesInfoList(userId);
    }

    /**
     * 新增或修改视频合集
     * @param videoIds
     * @param userVideoSeries
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void SaveUserVideoSeries(String videoIds, UserVideoSeries userVideoSeries) {
        if (userVideoSeries.getSeriesId() == null && videoIds == null) {
            throw new BusinessException(600, "参数错误");
        }
        //若为新建合集
        if (userVideoSeries.getSeriesId() == null) {
            CheckVideoIds(userVideoSeries.getUserId(), videoIds);
            String[] videoIdArray = videoIds.split(",");
            //设置视频次序
            userVideoSeries.setSort(this.userVideoSeriesMapper.GetMaxSeriesSort(userVideoSeries.getUserId()) + 1);
            this.userVideoSeriesMapper.InsertVideoSeries(userVideoSeries);
            //更新series_video表
            this.SaveSeriesVideoInfo(userVideoSeries.getUserId(), userVideoSeries.getSeriesId(), videoIds);
        }
        //更新合集
        else {
            UserVideoSeries dbuserVideoSeries = this.userVideoSeriesMapper.GetUserSeriesBySeriesId(userVideoSeries.getSeriesId());
            if (!userVideoSeries.getUserId().equals(dbuserVideoSeries.getUserId())) {
                throw new BusinessException(600, "无权限");
            }
            UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
            userVideoSeriesQuery.setSeriesId(userVideoSeries.getSeriesId());
            userVideoSeriesQuery.setSeriesName(userVideoSeries.getSeriesName());
            userVideoSeriesQuery.setSeriesDescription(userVideoSeries.getSeriesDescription());
            userVideoSeriesQuery.setUpdateTime(LocalDateTime.now());
            this.userVideoSeriesMapper.UpdateUserVideoSeries(userVideoSeriesQuery);
        }
    }

    /**
     * 检测视频是否为合集创建者发布的
     * @param userId
     * @param videoIds
     */
    protected void CheckVideoIds(String userId, String videoIds) {
        String[] videoIdArray = videoIds.split(",");
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setVideoIdArray(videoIdArray);
        Integer count = this.videoInfoMapper.GetVideoCount(videoInfoQuery);
        if (count != videoIdArray.length) {
            throw new BusinessException(600, "参数错误");
        }
    }

    /**
     * 保存该合集的视频信息
     *
     * @param userId
     * @param seriesId
     * @param videoIds
     */
    @Override
    public void SaveSeriesVideoInfo(String userId, Integer seriesId, String videoIds) {
        UserVideoSeries userVideoSeries = this.userVideoSeriesMapper.GetUserSeriesBySeriesId(seriesId);
        if (userVideoSeries == null || !userId.equals(userVideoSeries.getUserId())) {
            throw new BusinessException(600, "参数错误");
        }
        //视频信息是否合法
        this.CheckVideoIds(userId, videoIds);
        String[] videoIdArray = videoIds.split(",");
        Integer sort = this.seriesVideoMapper.GetMaxSortBySeriesId(seriesId);
        List<SeriesVideo> seriesVideoList = new ArrayList<>();
        for (String videoId: videoIdArray) {
            SeriesVideo seriesVideo = new SeriesVideo();
            seriesVideo.setSeriesId(seriesId);
            seriesVideo.setVideoId(videoId);
            seriesVideo.setSort(++sort);
            seriesVideo.setUserId(userId);
            seriesVideoList.add(seriesVideo);
        }
        //批量插入视频合集信息
        this.seriesVideoMapper.InsertBatch(seriesVideoList);
    }

    /**
     * 更新该合集的视频信息
     *
     * @param userId
     * @param seriesId
     * @param videoIds
     */
    @Override
    public void UpdateSeriesVideoInfo(String userId, Integer seriesId, String videoIds) {
        UserVideoSeries userVideoSeries = this.userVideoSeriesMapper.GetUserSeriesBySeriesId(seriesId);
        if (userVideoSeries == null || !userId.equals(userVideoSeries.getUserId())) {
            throw new BusinessException(600, "参数错误");
        }
        //视频信息是否合法
        this.CheckVideoIds(userId, videoIds);
        String[] videoIdArray = videoIds.split(",");
        Integer sort = this.seriesVideoMapper.GetMaxSortBySeriesId(seriesId);
        List<SeriesVideo> seriesVideoList = new ArrayList<>();
        for (String videoId: videoIdArray) {
            SeriesVideo seriesVideo = new SeriesVideo();
            seriesVideo.setSeriesId(seriesId);
            seriesVideo.setVideoId(videoId);
            seriesVideo.setSort(++sort);
            seriesVideo.setUserId(userId);
            seriesVideoList.add(seriesVideo);
        }
        //批量更新视频合集信息
        this.seriesVideoMapper.UpdateBatch(seriesVideoList);

    }

    /**
     * 根据合集id查询合集信息
     *
     * @param seriesId
     * @return
     */
    @Override
    public UserVideoSeries GetVideoSeriesBySeriesId(Integer seriesId) {
        return this.userVideoSeriesMapper.GetUserSeriesBySeriesId(seriesId);
    }

    /**
     * 删除集合中的视频
     *
     * @param seriesId
     * @param videoId
     * @param userId
     */
    @Override
    public void DeleteSeriesVideo(Integer seriesId, String videoId, String userId) {
        SeriesVideoQuery seriesVideoQuery = new SeriesVideoQuery();
        seriesVideoQuery.setVideoId(videoId);
        seriesVideoQuery.setUserId(userId);
        seriesVideoQuery.setSeriesId(seriesId);
        this.seriesVideoMapper.DeleteVideoBySeriesId(seriesVideoQuery);
    }

    /**
     * 删除视频集合
     *
     * @param seriesId
     * @param userId
     */
    @Override
    public void DeleteVideoSeriesBySeriesId(Integer seriesId, String userId) {
        UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
        userVideoSeriesQuery.setUserId(userId);
        userVideoSeriesQuery.setSeriesId(seriesId);
        //count记录是删除
        Integer count = this.userVideoSeriesMapper.DeleteUserVideoSeriesBySeriesId(userVideoSeriesQuery);
        if (count == 0) {
            throw new BusinessException(600, "参数错误");
        }
        //同时删除series_video表中合集的视频信息
        SeriesVideoQuery seriesVideoQuery = new SeriesVideoQuery();
        seriesVideoQuery.setSeriesId(seriesId);
        seriesVideoQuery.setUserId(userId);
        this.seriesVideoMapper.DeleteVideoBySeriesId(seriesVideoQuery);
    }

    /**
     * 获取视频合集以及合集的前五个视频
     *
     * @param userVideoSeriesQuery
     * @return
     */
    @Override
    public List<UserVideoSeries> GetVideoSeriesListWithVideo(UserVideoSeriesQuery userVideoSeriesQuery) {
        return this.userVideoSeriesMapper.GetVideoSeriesListWithVideo(userVideoSeriesQuery);
    }

    /**
     * 修改合集顺序
     *
     * @param seriesIds
     * @param userId
     */
    @Override
    public void ChangeVideoSeriesSort(String seriesIds, String userId) {
        String[] seriesIdArray = seriesIds.split(",");
        List<UserVideoSeries> userVideoSeriesList = new ArrayList<>();
        Integer sort = 0;
        for (String seriesId : seriesIdArray) {
            UserVideoSeries userVideoSeries = new UserVideoSeries();
            userVideoSeries.setUserId(userId);
            userVideoSeries.setSort(++sort);
            userVideoSeries.setSeriesId(Integer.parseInt(seriesId));
            userVideoSeriesList.add(userVideoSeries);
        }
        this.userVideoSeriesMapper.UpdateVideoSeriesSort(userVideoSeriesList);
    }
}
