package com.PiliPili.service.impl;

import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.mapper.VideoInfoFileMapper;
import com.PiliPili.service.VideoInfoFileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VideoInfoFileServiceImpl implements VideoInfoFileService {

    @Resource
    VideoInfoFileMapper videoInfoFileMapper;

    /**
     * 获取分p视频
     *
     * @param videoInfoQuery
     */
    @Override
    public List<VideoInfoFile> GetVideoPListByVideoId(VideoInfoQuery videoInfoQuery) {
        return this.videoInfoFileMapper.GetVideoPListByVideoId(videoInfoQuery);
    }

    /**
     * 获取视频文件
     *
     * @param fileId
     */
    @Override
    public VideoInfoFile GetVideoFileByFileId(String fileId) {
        return this.videoInfoFileMapper.GetVideoFileByFileId(fileId);
    }
}
