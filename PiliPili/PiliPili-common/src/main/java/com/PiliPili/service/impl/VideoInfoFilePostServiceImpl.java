package com.PiliPili.service.impl;

import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.query.VideoFilePostQuery;
import com.PiliPili.mapper.VideoInfoFilePostMapper;
import com.PiliPili.service.VideoInfoFilePostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import java.util.List;

@Service
public class VideoInfoFilePostServiceImpl implements VideoInfoFilePostService {

    @Resource
    private VideoInfoFilePostMapper videoInfoFilePostMapper;

    /**
     * 根据视频id获取视频分p信息
     *
     * @param videoFilePostQuery
     * @return
     */
    @Override
    public List<VideoInfoFilePost> SelectListByVideoId(VideoFilePostQuery videoFilePostQuery) {
        return this.videoInfoFilePostMapper.GetVideoInfoFilePostList(videoFilePostQuery);
    }
}
