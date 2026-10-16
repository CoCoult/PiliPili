package com.PiliPili.web.controller;


import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.annotation.RecordUserMessage;
import com.PiliPili.entity.enums.MessageEnum;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.pojo.VideoBarrage;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.VideoBarrageQuery;
import com.PiliPili.service.VideoBarrageService;
import com.PiliPili.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/barrage")
@Validated
@Slf4j
public class VideoBarrageController extends BaseController {
    @Resource
    VideoBarrageService videoBarrageService;

    @Resource
    VideoInfoService videoInfoService;

    /**
     * 发送弹幕
     *
     * @param text
     * @param videoId
     * @param fileId
     * @param mode
     * @param color
     * @param time
     * @return
     */
    @RequestMapping("/postBarrage")
    @GlobalInterceptor(checkLogin = true)
    public Result PostBarrage(@NotEmpty @Size(max = 300) String text,
                              @NotEmpty String videoId,
                              @NotEmpty String fileId,
                              @NotNull Integer mode,
                              @NotEmpty String color,
                              @NotNull Integer time) {
        VideoBarrage videoBarrage = new VideoBarrage();
        videoBarrage.setVideoId(videoId);
        videoBarrage.setFileId(fileId);
        videoBarrage.setText(text);
        videoBarrage.setMode(mode);
        videoBarrage.setColor(color);
        videoBarrage.setTime(time);
        videoBarrage.setUserId(GetTokenUserInfo().getUserId());
        videoBarrage.setPostTime(LocalDateTime.now());
        videoBarrageService.SaveVideoBarrage(videoBarrage);
        // 返回带自增 barrageId 的弹幕（前端拿它登记"已渲染"，避免增量拉取重复渲染自己刚发的）
        return Result.success(videoBarrage);
    }

    /**
     * 获取视频弹幕
     *
     * @param videoId
     * @param fileId
     * @return
     */
    @RequestMapping("/getBarrage")
    public Result GetBarrage(@NotEmpty String videoId, @NotEmpty String fileId) {
        VideoInfo videoInfo = videoInfoService.GetVideoInfoByVideoId(videoId);
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            return Result.success(new ArrayList<>());
        }
        VideoBarrageQuery videoBarrageQuery = new VideoBarrageQuery();
        videoBarrageQuery.setVideoId(videoId);
        videoBarrageQuery.setFileId(fileId);
        videoBarrageQuery.setOrderByField("barrage_id asc");
        List<VideoBarrage> videoBarrageList = videoBarrageService.GetVideoBarrageByVideoId(videoBarrageQuery);
        return Result.success(videoBarrageList);
    }

    /**
     * 增量拉取弹幕（前端定时轮询，只取新增的弹幕，避免每次全量重拉）
     * <p>
     * 游标是 (fromTime, fromId) 二元组：弹幕时间精确到秒，同一秒可能有多条，
     * 只用时间做游标会漏取同一秒内的后续弹幕。
     *
     * @param videoId  视频ID（用于判断弹幕开关）
     * @param fileId   分P ID
     * @param fromTime 已加载到的最大弹幕时间(秒)
     * @param fromId   该时间下已加载到的最大弹幕ID
     * @param toTime   拉取上界(秒)，不传默认 fromTime + 5 分钟
     * @param limit    单次返回上限，不传默认 200
     */
    @RequestMapping("/getBarrageIncrement")
    public Result GetBarrageIncrement(@NotEmpty String videoId, @NotEmpty String fileId,
                                     Integer fromTime, Integer fromId, Integer toTime, Integer limit) {
        VideoInfo videoInfo = videoInfoService.GetVideoInfoByVideoId(videoId);
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            return Result.success(new ArrayList<>());
        }
        List<VideoBarrage> videoBarrageList = videoBarrageService.GetVideoBarrageIncrement(fileId, fromTime, fromId, toTime, limit);
        return Result.success(videoBarrageList);
    }
}
