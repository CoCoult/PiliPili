package com.PiliPili.service;

import com.PiliPili.entity.page.Page;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoBarrage;
import com.PiliPili.entity.query.VideoBarrageQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VideoBarrageService {

    /**
     * 保存发送的弹幕
     */
    void SaveVideoBarrage(VideoBarrage videoBarrage);

    /**
     * 获取视频弹幕
     * @param videoBarrageQuery
     * @return
     */
    List<VideoBarrage> GetVideoBarrageByVideoId(VideoBarrageQuery videoBarrageQuery);

    /**
     * 查询弹幕
     * @param videoBarrageQuery
     * @return
     */
    PageBean GetBarrageByParam(VideoBarrageQuery videoBarrageQuery);

    /**
     * 删除弹幕
     * @param userId
     * @param barrageId
     */
    void DeleteBarrage(String userId, Integer barrageId);
}
