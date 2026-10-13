package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.query.VideoPlayHistoryQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.StatisticsInfoService;
import com.PiliPili.service.VideoPlayHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/history")
@Validated
public class VideoPlayHistoryController extends BaseController {

    @Resource
    VideoPlayHistoryService videoPlayHistoryService;

    @Resource
    StatisticsInfoService statisticsInfoService;

    /**
     * 获取用户的视频历史观看记录
     *
     * @param pageNum
     * @return
     */
    @RequestMapping("/getVideoHistory")
    @GlobalInterceptor(checkLogin = true)
    public Result GetVideoHistory(Integer pageNum) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setUserId(tokenUserInfoDto.getUserId());
        videoPlayHistoryQuery.setOrderByField("last_update_time desc");
        videoPlayHistoryQuery.setSelectVideoInfo(true);
        if (pageNum != null) {
            videoPlayHistoryQuery.setPageNumber(pageNum);
        }
        PageBean pageBean = this.videoPlayHistoryService.GetVideoPlayHistory(videoPlayHistoryQuery);
        return Result.success(pageBean);
    }

    /**
     * 清空历史观看记录
     *
     * @return
     */
    @RequestMapping("/cleanVideoHistory")
    @GlobalInterceptor(checkLogin = true)
    public Result CleanVideoHistory() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setUserId(tokenUserInfoDto.getUserId());
        this.videoPlayHistoryService.DeleteAllUserVideoPlayHistory(videoPlayHistoryQuery);
        return Result.success();
    }

    /**
     * 删除指定历史观看记录
     *
     * @param videoId
     * @return
     */
    @RequestMapping("/delVideoHistory")
    @GlobalInterceptor(checkLogin = true)
    public Result DelVideoHistory(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setUserId(tokenUserInfoDto.getUserId());
        videoPlayHistoryQuery.setVideoId(videoId);
        this.videoPlayHistoryService.DeleteUserVideoPlayHistoryByVideoId(videoPlayHistoryQuery);
        return Result.success();
    }

    @RequestMapping("/test")
    public Result test() {
        this.statisticsInfoService.StatisticData();
        return Result.success();
    }
}
