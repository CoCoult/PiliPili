package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.enums.VideoStatusEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.query.VideoFilePostQuery;
import com.PiliPili.entity.query.VideoInfoPostQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.vo.UserVideoCountResult;
import com.PiliPili.entity.vo.VideoInfoListResult;
import com.PiliPili.entity.vo.VideoInfoResult;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.service.VideoInfoFilePostService;
import com.PiliPili.service.VideoInfoPostService;
import com.PiliPili.service.VideoInfoService;
import com.PiliPili.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/videoCenter")
@Validated
@Slf4j
public class UserVideoPostCenterController extends BaseController {

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;

    //TODO 项目完成后测试接口

    /**
     * 用户上传视频(和修改视频接口复用）
     *
     * @param videoId
     * @param videoCover
     * @param videoName
     * @param pCategoryId
     * @param categoryId
     * @param postType
     * @param tags
     * @param introduction
     * @param interaction
     * @param uploadFileList
     * @return
     */
    @RequestMapping("/postVideo")
    @GlobalInterceptor(checkLogin = true)
    public Result PostVideo(String videoId,
                            @NotEmpty String videoCover,
                            @NotEmpty @Size(max = 100) String videoName,
                            @NotNull Integer pCategoryId,
                            Integer categoryId,
                            @NotNull Integer postType,
                            @NotEmpty @Size(max = 255) String tags,
                            @Size(max = 2000) String introduction,
                            @Size(max = 3) String interaction,
                            @NotEmpty String uploadFileList) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        //JSon格式uploadFileList转变为VideoInfoFilePost对象
        List<VideoInfoFilePost> videoInfoFilePostlist = JsonUtils.convertJsonArrayToList(uploadFileList, VideoInfoFilePost.class);
        //设置视频信息
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setVideoId(videoId);
        videoInfoPost.setVideoCover(videoCover);
        videoInfoPost.setVideoName(videoName);
        videoInfoPost.setpCategoryId(pCategoryId);
        videoInfoPost.setCategoryId(categoryId);
        videoInfoPost.setPostType(postType);
        videoInfoPost.setTags(tags);
        videoInfoPost.setIntroduction(introduction);
        videoInfoPost.setInteraction(interaction);
        videoInfoPost.setUserId(tokenUserInfoDto.getUserId());
        //保存视频
        videoInfoPostService.SaveVideoInfo(videoInfoPost, videoInfoFilePostlist);
        return Result.success();
    }

    /**
     * 获取发布的视频
     *
     * @param status
     * @param pageNum
     * @param videoNameFuzzy
     * @return
     */
    @RequestMapping("/getVideoPost")
    public Result GetVideoPost(Integer status, Integer pageNum, String videoNameFuzzy) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();

        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        if (pageNum != null) {
            videoInfoPostQuery.setPageNumber(pageNum);
        }
        videoInfoPostQuery.setOrderByField("v.create_time desc");
        if (status != null) {
            if (status == -1) {
                videoInfoPostQuery.setExcludeStatus(new Integer[]{VideoStatusEnum.APPROVED.getCode(), VideoStatusEnum.REJECTED.getCode()});
            } else {
                videoInfoPostQuery.setStatus(status);
            }
        }
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoPostQuery.setQueryCount(true);
        PageBean pageBean = videoInfoPostService.SelectVideoPage(videoInfoPostQuery);
        return Result.success(pageBean);
    }

    /**
     * 获取视频数量信息
     *
     * @return
     */
    @RequestMapping("/getVideoCountInfo")
    public Result GetVideoCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();

        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setStatus(VideoStatusEnum.APPROVED.getCode());
        Integer auditPassCount = videoInfoPostService.GetCountByUserId(videoInfoPostQuery);
        videoInfoPostQuery.setStatus(VideoStatusEnum.REJECTED.getCode());
        Integer failPassCount = videoInfoPostService.GetCountByUserId(videoInfoPostQuery);
        videoInfoPostQuery.setStatus(null);
        videoInfoPostQuery.setExcludeStatus(new Integer[]{VideoStatusEnum.APPROVED.getCode(), VideoStatusEnum.REJECTED.getCode()});
        Integer inProgressCount = videoInfoPostService.GetCountByUserId(videoInfoPostQuery);
        UserVideoCountResult userVideoCountResult = new UserVideoCountResult();
        userVideoCountResult.setAuditPassCount(auditPassCount);
        userVideoCountResult.setInProgressCount(inProgressCount);
        userVideoCountResult.setFailPassCount(failPassCount);
        return Result.success(userVideoCountResult);
    }

    /**
     * 根据视频id获取视频信息
     *
     * @param videoId
     * @return
     */
    @RequestMapping("/getVideoByVideoId")
    public Result GetVideoByVideoId(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();

        VideoInfoPost videoInfoPost = this.videoInfoPostService.GetVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null || !videoInfoPost.getUserId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException();
        }

        VideoFilePostQuery videoFilePostQuery = new VideoFilePostQuery();
        videoFilePostQuery.setVideoId(videoId);
        videoFilePostQuery.setOrderByField("file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = this.videoInfoFilePostService.SelectListByVideoId(videoFilePostQuery);
        VideoInfoListResult videoInfoListResult = new VideoInfoListResult();
        videoInfoListResult.setVideoInfo(videoInfoPost);
        videoInfoListResult.setVideoInfoFilePostList(videoInfoFilePostList);
        return Result.success(videoInfoListResult);
    }

    /**
     * 保存视频开关弹幕评论的行为
     *
     * @param videoId
     * @param interaction
     * @return
     */
    @RequestMapping("/saveVideoInteraction")
    public Result SaveVideoInteraction(@NotEmpty String videoId, String interaction) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        videoInfoService.ChangeInteraction(videoId, tokenUserInfoDto.getUserId(), interaction);
        return Result.success();
    }

    /**
     * 删除视频
     * @param videoId
     * @return
     */
    @RequestMapping("/deleteVideo")
    public Result DeleteVideo(@NotEmpty String videoId) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        videoInfoService.DeleteVideo(videoId, tokenUserInfoDto.getUserId());
        return Result.success();
    }
}
