package com.PiliPili.admin.controller;

import com.PiliPili.annotation.RecordUserMessage;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.enums.MessageEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.query.VideoFilePostQuery;
import com.PiliPili.entity.query.VideoInfoPostQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.VideoInfoFilePostService;
import com.PiliPili.service.VideoInfoPostService;
import com.PiliPili.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/videoInfo")
/*
  视频审核
 */
public class VideoInfoController extends BaseController {

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoService videoInfoService;

    //TODO
    /**
     * 获取发布的视频
     *
     */
    @RequestMapping("/getVideoPost")
    public Result GetVideoPost(VideoInfoPostQuery videoInfoPostQuery ) {
        videoInfoPostQuery.setOrderByField("v.last_update_time desc");
        videoInfoPostQuery.setQueryCount(true);
        videoInfoPostQuery.setQueryUserInfo(true);
        if (videoInfoPostQuery.getPageNumber() == null) {
            videoInfoPostQuery.setPageNumber(1);
        }
        if (videoInfoPostQuery.getPageSize() == null) {
            videoInfoPostQuery.setPageSize(10);
        }
        PageBean pageBean = videoInfoPostService.SelectVideoPage(videoInfoPostQuery);
        return Result.success(pageBean);
    }

    /**
     * 审核视频
     * @param videoId
     * @param status
     * @param reason
     * @return
     */
    @RequestMapping("/auditVideo")
    @RecordUserMessage(messageType = MessageEnum.SYSTEM)
    public Result AuditVideo(@NotEmpty String videoId, @NotNull Integer status, @NotEmpty String reason) {
        videoInfoPostService.AuditVideo(videoId, status, reason);
        return Result.success();
    }

    /**
     * 删除视频
     * @param videoId
     * @return
     */
    @RequestMapping("/deleteVideo")
    public Result DelVideo(@NotEmpty String videoId) {
        this.videoInfoService.DeleteVideo(videoId, null);
        return Result.success();
    }

    @RequestMapping("/getVideoPList")
    public Result GetVideoPList(@NotEmpty String videoId) {
        VideoFilePostQuery videoFilePostQuery = new VideoFilePostQuery();
        videoFilePostQuery.setOrderByField("file_index asc");
        videoFilePostQuery.setVideoId(videoId);
        List<VideoInfoFilePost> videoInfoFilePostList = this.videoInfoFilePostService.SelectListByVideoId(videoFilePostQuery);
        return Result.success(videoInfoFilePostList);
    }
}
