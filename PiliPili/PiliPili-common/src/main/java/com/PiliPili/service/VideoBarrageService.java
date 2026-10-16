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
     * 增量拉取弹幕（客户端带 (时间,ID) 游标轮询，只取新增部分）
     *
     * @param fileId   分P ID
     * @param fromTime 已加载到的最大弹幕时间(秒)
     * @param fromId   该时间下已加载到的最大弹幕ID
     * @param toTime   拉取上界(秒)，不传则默认 fromTime + 一个分片时长
     * @param limit    单次返回上限
     */
    List<VideoBarrage> GetVideoBarrageIncrement(String fileId, Integer fromTime, Integer fromId, Integer toTime, Integer limit);

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
