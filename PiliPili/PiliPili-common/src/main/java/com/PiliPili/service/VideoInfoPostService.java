package com.PiliPili.service;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.query.VideoInfoPostQuery;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public interface VideoInfoPostService {

    /**
     * 保存视频
     * @param videoInfoPost
     * @param videoInfoPostList
     */
    public void SaveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> videoInfoPostList);

    /**
     * 视频转码
     * @param videoInfoFilePost
     */
    void TransferVideoFile(VideoInfoFilePost videoInfoFilePost);

    /**
     * 分页查询视频数据
     * @param videoInfoPostQuery
     * @return
     */
    PageBean SelectVideoPage(VideoInfoPostQuery videoInfoPostQuery);

    /**
     * 获取用某状态的视频
     * @param videoInfoPostQuery
     * @return
     */
    Integer GetCountByUserId(VideoInfoPostQuery videoInfoPostQuery);

    /**
     * 审核视频
     * @param videoId
     * @param status
     * @param reason
     */
    void AuditVideo(String videoId, Integer status, String reason);

    /**
     * 根据视频id获取视频信息
     */
    VideoInfoPost GetVideoInfoPostByVideoId(String videoId);

}
