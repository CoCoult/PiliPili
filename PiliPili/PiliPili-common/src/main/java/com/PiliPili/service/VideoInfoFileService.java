package com.PiliPili.service;

import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.query.VideoInfoQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VideoInfoFileService {
    /**
     * 获取分p视频
     */
    List<VideoInfoFile> GetVideoPListByVideoId(VideoInfoQuery videoInfoQuery);

    /**
     * 获取视频文件
     */
    VideoInfoFile GetVideoFileByFileId(String fileId);

}
