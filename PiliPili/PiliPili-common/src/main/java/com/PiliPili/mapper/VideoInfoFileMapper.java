package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.query.VideoInfoFileQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoInfoFileMapper {

    /**
     * 获取视频分p
     */
    List<VideoInfoFile> GetVideoPListByVideoId(VideoInfoQuery videoInfoQuery);

    /**
     * 获取视频文件信息
     */
    VideoInfoFile GetVideoFileByFileId(String fileId);

    /**
     * 根据id删除视频文件信息
     * @param videoInfoFileQuery
     */
    void deleteByVideoId(@Param("videoInfoFileQuery") VideoInfoFileQuery videoInfoFileQuery);

    /**
     * 批量插入
     * @param videoInfoFileList
     */
    void InsertBatch(@Param("videoInfoFileList") List<VideoInfoFile> videoInfoFileList);

}
