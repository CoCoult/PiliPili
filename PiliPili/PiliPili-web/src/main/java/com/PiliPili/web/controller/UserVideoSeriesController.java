package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.pojo.SeriesVideo;
import com.PiliPili.entity.pojo.UserVideoSeries;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.SeriesVideoQuery;
import com.PiliPili.entity.query.UserVideoSeriesQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.vo.SeriesVideoInfoResult;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.service.SeriesVideoService;
import com.PiliPili.service.UserVideoSeriesService;
import com.PiliPili.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/userCenter/series")
@Validated
@Slf4j
public class UserVideoSeriesController extends BaseController{

    @Resource
    UserVideoSeriesService userVideoSeriesService;

    @Resource
    SeriesVideoService seriesVideoService;

    @Resource
    VideoInfoService videoInfoService;

    /**
     * 获取全部视频集合并返回集合中第一条视频的封面
     * @param userId
     * @return
     */
    @RequestMapping("/getVideoSeries")
    public Result GetVideoSeries(@NotEmpty String userId){
        List<UserVideoSeries> userVideoSeriesList = this.userVideoSeriesService.GetSeriesInfoList(userId);
        return Result.success(userVideoSeriesList);
    }

    /**
     * 新增或修改分类
     * @param seriesId
     * @param seriesName
     * @param seriesDescription
     * @param videoIds
     * @return
     */
    @RequestMapping("/saveVideoSeries")
    @GlobalInterceptor(checkLogin = true)
    public Result SaveVideoSeries(Integer seriesId,
                                  @NotEmpty @Size(max = 100) String seriesName,
                                  @Size(max = 200) String seriesDescription,
                                  String videoIds){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        UserVideoSeries userVideoSeries = new UserVideoSeries();
        userVideoSeries.setUserId(tokenUserInfoDto.getUserId());
        userVideoSeries.setSeriesName(seriesName);
        userVideoSeries.setSeriesId(seriesId);
        userVideoSeries.setSeriesDescription(seriesDescription);
        this.userVideoSeriesService.SaveUserVideoSeries(videoIds, userVideoSeries);
        return Result.success();
    }

    /**
     * 获取未加入当前集合的视频
     * @param seriesId
     * @return
     */
    @RequestMapping("/getAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result GetVideoSeries(Integer seriesId){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (seriesId != null) {
            SeriesVideoQuery seriesVideoQuery = new SeriesVideoQuery();
            seriesVideoQuery.setSeriesId(seriesId);
            List<SeriesVideo> seriesVideoList = this.seriesVideoService.GetVideoListBySeriesId(seriesVideoQuery);
            List<String> videoIdList = seriesVideoList.stream().map(item -> item.getVideoId()).collect(Collectors.toList());
            //排除已在合集的视频id
            videoInfoQuery.setExcludeVideoId(videoIdList.toArray(new String[videoIdList.size()]));
        }
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoQuery.setOrderByField("create_time desc");
        List<VideoInfo> videoInfoList = this.videoInfoService.GetVideoListByUserId(videoInfoQuery);
        return Result.success(videoInfoList);
    }

    /**
     * 获取合集内的视频详情
     * @param seriesId
     * @return
     */
    @RequestMapping("/getSeriesVideoInfo")
    public Result GetSeriesVideoInfo(@NotNull Integer seriesId){
        UserVideoSeries userVideoSeries = this.userVideoSeriesService.GetVideoSeriesBySeriesId(seriesId);
        if (userVideoSeries == null) {
            throw new BusinessException();
        }
        //查询合集内的视频信息
        SeriesVideoQuery seriesVideoQuery = new SeriesVideoQuery();
        seriesVideoQuery.setSeriesId(seriesId);
        seriesVideoQuery.setOrderByField("sort asc");
        seriesVideoQuery.setSelectVideoInfo(true);
        List<SeriesVideo> seriesVideoList = this.seriesVideoService.GetVideoListBySeriesId(seriesVideoQuery);
        SeriesVideoInfoResult seriesVideoInfoResult = new SeriesVideoInfoResult();
        //返回合集信息以及合集内视频信息
        seriesVideoInfoResult.setUserVideoSeries(userVideoSeries);
        seriesVideoInfoResult.setSeriesVideoList(seriesVideoList);
        return Result.success(seriesVideoInfoResult);
    }

    /**
     * 将视频添加到合集
     * @param seriesId
     * @param videoIds
     * @return
     */
    @RequestMapping("/saveSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result SaveSeriesVideo(@NotNull Integer seriesId,@NotEmpty String videoIds){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.userVideoSeriesService.SaveSeriesVideoInfo(tokenUserInfoDto.getUserId(), seriesId,videoIds);
        return Result.success();
    }

    /**
     * 删除集合中的视频
     * @param seriesId
     * @param videoId
     * @return
     */
    @RequestMapping("/delSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result DeleteSeriesVideo(@NotNull Integer seriesId, @NotEmpty String videoId){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.userVideoSeriesService.DeleteSeriesVideo(seriesId, videoId, tokenUserInfoDto.getUserId());
        return Result.success();
    }

    /**
     * 删除视频集合
      * @param seriesId
     * @return
     */
    @RequestMapping("/delVideoSeries")
    @GlobalInterceptor(checkLogin = true)
    public Result DeleteSeriesVideo(@NotNull Integer seriesId){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.userVideoSeriesService.DeleteVideoSeriesBySeriesId(seriesId, tokenUserInfoDto.getUserId());
        return Result.success();
    }

    /**
     * 修改合集顺序
     * @param seriesIds
     * @return
     */
    @RequestMapping("/changeVideoSeriesSort")
    public Result ChangeVideoSeriesSort(@NotNull String seriesIds){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.userVideoSeriesService.ChangeVideoSeriesSort(seriesIds, tokenUserInfoDto.getUserId());
        return Result.success();
    }

    /**
     * 更新视频集合信息
     * @param seriesId
     * @return
     */
    @RequestMapping("/updateSeriesVideoInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result updateSeriesVideoInfo(@NotNull Integer seriesId, String videoIds){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.userVideoSeriesService.UpdateSeriesVideoInfo(tokenUserInfoDto.getUserId(), seriesId, videoIds);
        return Result.success();
    }

    /**
     * 获取指定视频分类以及分类的前五个视频
     * @param userId
     * @return
     */
    @RequestMapping("/getSeriesWithVideo")
    public Result updateSeriesVideoInfo(@NotEmpty String userId){
        UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
        userVideoSeriesQuery.setUserId(userId);
        userVideoSeriesQuery.setOrderByField("sort asc");
        List<UserVideoSeries> userVideoSeriesList = this.userVideoSeriesService.GetVideoSeriesListWithVideo(userVideoSeriesQuery);
        return Result.success(userVideoSeriesList);
    }

}
