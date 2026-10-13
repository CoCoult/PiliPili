package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.query.VideoFilePostQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoInfoFilePostMapper {

    public List<VideoInfoFilePost> GetVideoInfoFilePostList(VideoFilePostQuery videoFilePostQuery);

    /**
     * 根据文件Id删除当前用户的文件
     *
     * @param FileIdList
     * @param userId
     */
    public void DeleteFileByIdList(List<String> FileIdList, String userId);

    /**
     * 插入视频分P文件信息
     */
    public void InsertVideoFilePost(VideoInfoFilePost videoInfoFilePost);

    /**
     * 更新视频分P文件信息
     */
    public void UpdateVideoFilePost(VideoInfoFilePost videoInfoFilePost);

    /**
     * 根据id更新视频上传表
     */
    void UpdateVideoFilePostByUploadIdAndUserId(@Param("videoInfoFilePost") VideoInfoFilePost videoInfoFilePost, @Param("uploadId") String uploadId, @Param("userId") String userId);

    Integer SelectCount(@Param("videoInfoFilePost") VideoInfoFilePost videoInfoFilePost);

    /**
     * 求视频总时长
     *
     * @param videoId
     * @return
     */
    Integer SumDuration(@Param("videoId") String videoId);

    /**
     *
     */
    void updateByParams(@Param("videoInfoFilePost") VideoInfoFilePost videoInfoFilePost, @Param("videoFilePostQuery") VideoFilePostQuery videoFilePostQuery);

    /**
     * 删除视频分p
     * @param query
     */
    void DeleteByVideoId(@Param("query") VideoFilePostQuery query);
}
