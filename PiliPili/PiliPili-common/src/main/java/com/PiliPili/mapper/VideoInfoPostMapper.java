package com.PiliPili.mapper;

import com.PiliPili.entity.dto.VideoInfoPostDTO;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.query.VideoFilePostQuery;
import com.PiliPili.entity.query.VideoInfoPostQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoInfoPostMapper {

    public VideoInfoPost GetVideoPostInfoByVideoId(String videoId);

    public void InsertVideoInfoPost(VideoInfoPost videoInfoPost);

    public void UpdateVideoInfoPost(VideoInfoPost videoInfoPost);

    /**
     * 更新视频审核表信息
     */
    void UpdateByVideoId(@Param("videoInfoPost") VideoInfoPost videoInfoPost, @Param("videoId") String videoId);

    /**
     * 获取当前状态的视频
     * @param videoInfoQuery
     * @return
     */
    List<VideoInfoPostDTO> SelectVideoByStatus(@Param("videoInfoQuery") VideoInfoPostQuery videoInfoQuery);

    /**
     * 获取当前状态的视频数量
     */
    Integer SelectCountByUserIdAndStatus(@Param("query") VideoInfoPostQuery query);

    /**
     * 更新信息
     * @param videoInfoPost
     * @param videoInfoPostQuery
     * @return
     */
    Integer updateByParams(@Param("videoInfoPost") VideoInfoPost videoInfoPost, @Param("videoInfoPostQuery") VideoInfoPostQuery videoInfoPostQuery);

    /**
     * 根据videoId选择视频信息
     * @param videoId
     */
    VideoInfoPost selectByVideoId(@Param("videoId") String videoId);

    /**
     * 删除视频
     * @param videoId
     */
    void DeleteByVideoId(String videoId);
}
