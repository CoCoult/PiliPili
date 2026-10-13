package com.PiliPili.service.impl;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.enums.SearchTypeOrderEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoBarrage;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.VideoBarrageQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.VideoBarrageMapper;
import com.PiliPili.mapper.VideoInfoMapper;
import com.PiliPili.service.VideoBarrageService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VideoBarrageServiceImpl implements VideoBarrageService {

    @Resource
    VideoBarrageMapper videoBarrageMapper;

    @Resource
    VideoInfoMapper videoInfoMapper;

    @Resource
    EsSearchComponent esSearchComponent;

    /**
     * 保存发送的弹幕
     *
     * @param videoBarrage
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void SaveVideoBarrage(VideoBarrage videoBarrage) {
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoBarrage.getVideoId());
        if (videoInfo == null){
            throw new BusinessException(600, "参数错误");
        }
        //interaction字段包含1则说明视频关闭弹幕功能
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            throw new BusinessException("该视频已关闭弹幕功能");
        }
        this.videoBarrageMapper.InsertVideoBarrage(videoBarrage);
        //更新视频弹幕数
        this.videoInfoMapper.UpdateVideoCount(videoInfo.getVideoId(), 1, "barrage_count");
        //TODO 未测试 更新es 弹幕数
        esSearchComponent.UpdateDocCount(videoBarrage.getVideoId(), SearchTypeOrderEnum.VIDEO_BARRAGE.getField(), 1);
    }

    /**
     * 获取视频弹幕
     *
     * @param videoBarrageQuery
     * @return
     */
    @Override
    public List<VideoBarrage> GetVideoBarrageByVideoId(VideoBarrageQuery videoBarrageQuery) {
        return this.videoBarrageMapper.GetVideoBarrageByVideoId(videoBarrageQuery);
    }

    /**
     * 查询弹幕
     *
     * @param videoBarrageQuery
     * @return
     */
    @Override
    public PageBean GetBarrageByParam(VideoBarrageQuery videoBarrageQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoBarrageQuery.getPageNumber(), videoBarrageQuery.getPageSize());
        Page<VideoBarrage> videoBarragePage = (Page<VideoBarrage>) this.videoBarrageMapper.SelectByParam(videoBarrageQuery);
        pageBean.setPageSize(videoBarrageQuery.getPageSize());
        pageBean.setPageNum(videoBarrageQuery.getPageNumber());
        pageBean.setPageTotal(videoBarragePage.getPages());
        pageBean.setRows(videoBarragePage.getResult());
        pageBean.setTotal(videoBarragePage.getTotal());
        return pageBean;
    }

    /**
     * 删除弹幕
     *
     * @param userId
     * @param barrageId
     */
    @Override
    public void DeleteBarrage(String userId, Integer barrageId) {
        VideoBarrage videoBarrage = this.videoBarrageMapper.SelectByBarrageId(barrageId);
        if (videoBarrage == null) {
            throw new BusinessException("参数错误");
        }
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoBarrage.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException("参数错误");
        }
        if (userId != null && !videoInfo.getUserId().equals(userId)) {
            throw new BusinessException("参数错误");
        }
        this.videoBarrageMapper.DeleteByBarrageId(barrageId);
    }
}
