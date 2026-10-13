package com.PiliPili.web.controller;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.component.RecommendComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.enums.SearchTypeOrderEnum;
import com.PiliPili.entity.enums.VideoRecommendEnum;
import com.PiliPili.entity.page.Page;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserAction;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.query.UserActionQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.vo.VideoInfoResult;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.service.UserActionService;
import com.PiliPili.service.VideoInfoFileService;
import com.PiliPili.service.VideoInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频展示controller
 */
@RestController
@RequestMapping("/video")
@Validated
public class VideoController extends BaseController {
    @Resource
    VideoInfoService videoInfoService;

    @Resource
    VideoInfoFileService videoInfoFileService;

    @Resource
    UserActionService userActionService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private RecommendComponent recommendComponent;

    /**
     * 获取网站推荐的视频
     *
     * @return
     */
    @RequestMapping("/getRecommendVideo")
    public Result GetRecommendVideo() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setGetUserInfo(true);
        List<VideoInfo> videoInfoList = videoInfoService.GetRecommendVideo(videoInfoQuery);
        return Result.success(videoInfoList);
    }

    /**
     * 获取网站视频
     *
     * @return
     */
    @RequestMapping("/getVideo")
    public Result GetVideo(Integer pCategoryId, Integer categoryId, Integer pageNum) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setGetUserInfo(true);
        if (pageNum != null) {
            videoInfoQuery.setPageNumber(pageNum);
        }
        videoInfoQuery.setPCategoryId(pCategoryId);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setOrderByField("vinfo.create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendEnum.NOT_RECOMMEND.getCode());
        PageBean videoInfoList = videoInfoService.GetVideo(videoInfoQuery);
        return Result.success(videoInfoList);
    }

    /**
     * 获取视频点赞投币收藏详情
     *
     * @return
     */
    @RequestMapping("/getVideoInfo")
    public Result GetVideoInfo(@NotEmpty String videoId) {
        VideoInfo videoInfo = this.videoInfoService.GetVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(404, "请求错误");
        }
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        if (tokenUserInfoDto == null) {
            return Result.success(null);
        } else {
            List<UserAction> userAction;
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setVideoId(videoId);
            userActionQuery.setUserId(tokenUserInfoDto.getUserId());
            userActionQuery.setActionType(new Integer[]{ActionTypeEnum.LIKE_VIDEO.getType(), ActionTypeEnum.COLLECT_VIDEO.getType(), ActionTypeEnum.COIN_VIDEO.getType()});
            userAction = this.userActionService.GetUserActionByVideoId(userActionQuery);
            VideoInfoResult videoInfoResult = new VideoInfoResult(videoInfo, userAction);
            return Result.success(videoInfoResult);
        }
    }

    /**
     * 获取视频分p
     *
     * @return
     */
    @RequestMapping("/getVideoPList")
    public Result GetVideoPList(@NotEmpty String videoId) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setOrderByField("file_index");
        List<VideoInfoFile> fileList = this.videoInfoFileService.GetVideoPListByVideoId(videoInfoQuery);
        return Result.success(fileList);
    }

    @RequestMapping("/reportVideoPlayOnline")
    public Result ReportVideoPlayOnline(@NotEmpty String fileId, @NotEmpty String deviceId) {
        return Result.success(redisComponent.ReportVideoPlayOnline(fileId, deviceId));
    }

    /**
     * 视频搜索
     *
     * @param keyword
     * @param pageNum
     * @param orderType
     * @return
     */
    @RequestMapping("/search")
    public Result VideoEsSearch(@NotEmpty String keyword, Integer pageNum, Integer orderType) {
        //记录搜索热词,存入redis中，每次搜索加一
        redisComponent.addVideoSearchCount(keyword);
        Page<VideoInfo> videoInfoPage = esSearchComponent.search(true, keyword, orderType, pageNum, 20);
        PageBean pageBean = new PageBean();
        pageBean.setTotal(videoInfoPage.getTotalCount());
        pageBean.setRows(videoInfoPage.getData());
        pageBean.setPageSize(videoInfoPage.getPageSize());
        pageBean.setPageNum(videoInfoPage.getPageNum());
        pageBean.setPageTotal(videoInfoPage.GetPages());
        return Result.success(pageBean);
    }

    /**
     * 获取视频观看区右侧的推荐列表
     *
     * @param keyword
     * @param videoId
     * @return
     */
    @RequestMapping("/getVideoRecommend")
    public Result GetVideoRecommend(@NotEmpty String keyword, @NotEmpty String videoId) {
        Page<VideoInfo> videoInfoPage = esSearchComponent.search(false, keyword, SearchTypeOrderEnum.VIDEO_PLAY.getType(), 1, 20);
        videoInfoPage.getData().stream().filter(item -> !item.getVideoId().equals(videoId)).collect(Collectors.toList());
        PageBean pageBean = new PageBean();
        pageBean.setTotal(videoInfoPage.getTotalCount());
        pageBean.setRows(videoInfoPage.getData());
        return Result.success(pageBean);
    }

    /**
     * 获取前十个搜索热词
     *
     * @return
     */
    @RequestMapping("/getSearchKeywordPop")
    public Result GetSearchKeywordPop() {
        List<String> keywords = redisComponent.GetKeywordPop(Constants.LENGTH_10);
        return Result.success(keywords);
    }

    @RequestMapping("/getHotVideoList")
    public Result GetHotVideoList(Integer pageNum) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (pageNum != null) {
            videoInfoQuery.setPageNumber(pageNum);
        }
        videoInfoQuery.setGetUserInfo(true);
        videoInfoQuery.setOrderByField("play_count desc");
        videoInfoQuery.setLastPlayHour(24);
        PageBean pageBean = this.videoInfoService.GetVideo(videoInfoQuery);

        return Result.success(pageBean);
    }
}
