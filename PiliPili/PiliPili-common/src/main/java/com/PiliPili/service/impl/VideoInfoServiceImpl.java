package com.PiliPili.service.impl;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.SysSettingDto;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.query.*;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.*;
import com.PiliPili.service.VideoInfoService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class VideoInfoServiceImpl implements VideoInfoService {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserMapper userMapper;
    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private VideoInfoMapper videoInfoMapper;

    @Resource
    private VideoInfoFileMapper videoInfoFileMapper;

    @Resource
    private VideoInfoPostMapper videoInfoPostMapper;

    @Resource
    private VideoInfoFilePostMapper videoInfoFilePostMapper;

    @Resource
    private VideoBarrageMapper videoBarrageMapper;

    @Resource
    private VideoCommentMapper videoCommentMapper;

    /**
     * 获取推荐视频
     */
    @Override
    public List<VideoInfo> GetRecommendVideo(VideoInfoQuery videoInfoQuery) {
        return this.videoInfoMapper.GetRecommendVideo(videoInfoQuery);
    }

    /**
     * 获取分类视频
     *
     * @param videoInfoQuery
     */
    @Override
    public PageBean GetVideo(VideoInfoQuery videoInfoQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoInfoQuery.getPageNumber(), videoInfoQuery.getPageSize());
        List<VideoInfo> videoInfoList = this.videoInfoMapper.GetVideo(videoInfoQuery);
        PageInfo<VideoInfo> pageList = new PageInfo<>(videoInfoList);
        pageBean.setRows(pageList.getList());
        pageBean.setTotal(pageList.getTotal());
        pageBean.setPageNum(videoInfoQuery.getPageNumber());
        pageBean.setPageSize(videoInfoQuery.getPageSize());
        pageBean.setPageTotal(pageList.getPages());
        return pageBean;
    }

    /**
     * 根据视频id获取视频信息
     *
     * @param videoId
     */
    @Override
    public VideoInfo GetVideoInfoByVideoId(String videoId) {
        return this.videoInfoMapper.GetVideoInfoByVideoId(videoId);
    }

    /**
     * 获取视频列表
     *
     * @param videoInfoQuery
     * @return
     */
    @Override
    public PageBean GetVideoList(VideoInfoQuery videoInfoQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoInfoQuery.getPageNumber(), videoInfoQuery.getPageSize());
        List<VideoInfo> videoInfoList = this.videoInfoMapper.GetVideoList(videoInfoQuery);
        Page<VideoInfo> pageInfo = (Page<VideoInfo>) videoInfoList;
        pageBean.setRows(pageInfo.getResult());
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setPageSize(videoInfoQuery.getPageSize());
        pageBean.setPageNum(videoInfoQuery.getPageNumber());
        pageBean.setPageTotal(pageInfo.getPages());
        return pageBean;
    }

    /**
     * 获取该用户下的过滤过的视频
     *
     * @param videoInfoQuery
     * @return
     */
    @Override
    public List<VideoInfo> GetVideoListByUserId(VideoInfoQuery videoInfoQuery) {
        return this.videoInfoMapper.GetVideoList(videoInfoQuery);
    }

    /**
     * 获取分类下的视频数
     *
     * @param videoInfoQuery
     * @return
     */
    @Override
    public Integer GetVideoCount(VideoInfoQuery videoInfoQuery) {
        return this.videoInfoMapper.GetVideoCountByCategoryId(videoInfoQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ChangeInteraction(String videoId, String userId, String interaction) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setInteraction(interaction);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setUserId(userId);
        videoInfoMapper.UpdateByParam(videoInfo, videoInfoQuery);

        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setInteraction(interaction);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostQuery.setUserId(userId);
        videoInfoPostMapper.updateByParams(videoInfoPost, videoInfoPostQuery);
    }

    /**
     * 删除视频
     *
     * @param videoId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void DeleteVideo(String videoId, String userId) {
        VideoInfo videoInfo = videoInfoMapper.GetVideoInfoByVideoId(videoId);

        if (videoInfo == null || userId != null && !userId.equals(videoInfo.getUserId())) {
            throw new BusinessException("参数错误");
        }

        this.videoInfoMapper.DeleteByVideoId(videoId);

        this.videoInfoPostMapper.DeleteByVideoId(videoId);

        SysSettingDto sysSettingDto = redisComponent.GetSysSettingInfo();
        this.userMapper.UpdateUserCount(videoInfo.getUserId(), -sysSettingDto.getPostVideoCoinCount(), "current_coin");

        //TODO 删除ES信息


        executorService.execute(() -> {
            VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
            videoInfoQuery.setVideoId(videoId);

            VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
            videoInfoFileQuery.setVideoId(videoId);
            //删除正式视频分p
            this.videoInfoFileMapper.deleteByVideoId(videoInfoFileQuery);

            VideoFilePostQuery videoFilePostQuery = new VideoFilePostQuery();
            videoFilePostQuery.setVideoId(videoId);
            //删除发布视频表分p
            this.videoInfoFilePostMapper.DeleteByVideoId(videoFilePostQuery);

            //删除弹幕
            VideoBarrageQuery videoBarrageQuery = new VideoBarrageQuery();
            videoBarrageQuery.setVideoId(videoId);
            this.videoBarrageMapper.DeleteByParam(videoBarrageQuery);

            //删除评论
            this.videoCommentMapper.DeleteCommentByVideoId(videoId);

            List<VideoInfoFile> videoInfoFileList = this.videoInfoFileMapper.GetVideoPListByVideoId(videoInfoQuery);
            for (VideoInfoFile videoInfoFile : videoInfoFileList) {
                try {
                    FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + videoInfoFile.getFilePath()));
                } catch (IOException e) {
                    log.error("删除文件失败，文件路径：{}", videoInfoFile.getFilePath());
                }
            }
        });
    }

    /**
     * 增加视频播放量
     *
     * @param videoId
     */
    @Override
    public void addVideoPlayCount(String videoId) {
        this.videoInfoMapper.UpdateVideoCount(videoId, 1, ActionTypeEnum.VIDEO_PLAY.getField());
    }

    //TODO 播放历史 17：08 视频模块

    //TODO 删除视频 搜索热词 8：24
}
