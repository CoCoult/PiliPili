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

    /**
     * 按"分P + 时间区间"查询弹幕（分片缓存回源用）
     * <p>
     * 时间区间为左闭右开：[timeMin, timeMax)。
     * 必须依赖索引 idx_file_time(file_id, time, barrage_id)，否则会退化成 filesort。
     *
     * @param fileId  分P ID
     * @param timeMin 起始秒（含）
     * @param timeMax 结束秒（不含）
     */
    List<VideoBarrage> SelectByFileIdAndTimeRange(@Param("fileId") String fileId,
                                                  @Param("timeMin") Integer timeMin,
                                                  @Param("timeMax") Integer timeMax);

    /**
     * 查询某个分P弹幕的最大出现时间（用于确定分片扫描范围）
     *
     * @return 没有弹幕时返回 null
     */
    Integer SelectMaxTimeByFileId(@Param("fileId") String fileId);

}
