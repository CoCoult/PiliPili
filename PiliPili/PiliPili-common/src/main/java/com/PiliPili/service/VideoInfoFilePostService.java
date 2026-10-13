package com.PiliPili.service;

import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.query.VideoFilePostQuery;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Size;
import java.util.List;

@Service
public interface VideoInfoFilePostService {

    /**
     * 根据视频id获取视频分p信息
     * @param videoFilePostQuery
     * @return
     */
    List<VideoInfoFilePost> SelectListByVideoId(VideoFilePostQuery videoFilePostQuery);
}
