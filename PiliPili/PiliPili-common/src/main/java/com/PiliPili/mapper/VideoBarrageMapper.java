package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.VideoBarrage;
import com.PiliPili.entity.query.VideoBarrageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoBarrageMapper {

    /**
     * 保存视频弹幕
     * @param videoBarrage
     */
    void InsertVideoBarrage(VideoBarrage videoBarrage);

    /**
     * 获取视频的弹幕
     * @param videoBarrageQuery
     * @return
     */
    List<VideoBarrage> GetVideoBarrageByVideoId(VideoBarrageQuery videoBarrageQuery);

    /**
     * 删除弹幕
     * @param query
     */
    void DeleteByParam(@Param("query") VideoBarrageQuery query);

    /**
     * 查询弹幕
     * @param query
     * @return
     */
    List<VideoBarrage> SelectByParam(@Param("query") VideoBarrageQuery query);

    /**
     * 根据id查弹幕
     * @param barrageId
     * @return
     */
    VideoBarrage SelectByBarrageId(@Param("barrageId") Integer barrageId);

    /**
     * 根据id删除弹幕
     * @param barrageId
     */
    void DeleteByBarrageId(@Param("barrageId") Integer barrageId);

    /**
     * 获取用户发布的弹幕数
     * @param userId
     * @return
     */
    Integer GetVideoBarrageCountByUserId(@Param("userId") String userId, @Param("videoId") String videoId);

}
