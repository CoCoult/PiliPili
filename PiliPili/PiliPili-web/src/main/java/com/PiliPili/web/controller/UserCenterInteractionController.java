package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.query.CommentQuery;
import com.PiliPili.entity.query.VideoBarrageQuery;
import com.PiliPili.entity.query.VideoInfoPostQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.*;
import com.PiliPili.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/userCenter")
@Validated
@Slf4j
public class UserCenterInteractionController extends BaseController{
    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoBarrageService videoBarrageService;


    /**
     * 获取当前用户的所有视频
     * @return
     */
    @RequestMapping("/loadAllVideo")
    public Result PostVideo() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoQuery.setOrderByField("create_time desc");
        List<VideoInfo> videoInfoList = this.videoInfoService.GetVideoListByUserId(videoInfoQuery);
        return Result.success(videoInfoList);
    }

    /**
     * 获取视频评论
     * @param pageNum
     * @param videoId
     * @return
     */
    @RequestMapping("/loadComment")
    public Result PostVideo(Integer pageNum, String videoId) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setVideoId(videoId);
        commentQuery.setVideoUserId(tokenUserInfoDto.getUserId());
        commentQuery.setOrderByField("comment_id desc");
        if(pageNum != null) {
            commentQuery.setPageNumber(pageNum);
        }
        commentQuery.setQueryVideoInfo(true);
        PageBean pageBean = this.videoCommentService.GetCommentByPage(commentQuery);
        return Result.success(pageBean);
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @RequestMapping("/delComment")
    public Result PostVideo(@NotNull Integer commentId) {
        videoCommentService.DeleteComment(commentId, GetTokenUserInfo().getUserId());
        return Result.success(null);
    }

    /**
     * 查询弹幕
     * @param pageNum
     * @param videoId
     * @return
     */
    @RequestMapping("/loadDanmu")
    public Result LoadBarrage(Integer pageNum, String videoId) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        VideoBarrageQuery videoBarrageQuery = new VideoBarrageQuery();
        videoBarrageQuery.setVideoId(videoId);
        if (pageNum != null) {
            videoBarrageQuery.setPageNumber(pageNum);
        }
        videoBarrageQuery.setVideoUserId(tokenUserInfoDto.getUserId());
        videoBarrageQuery.setOrderByField("barrage_id desc");
        videoBarrageQuery.setQueryVideoInfo(true);
        PageBean pageBean = this.videoBarrageService.GetBarrageByParam(videoBarrageQuery);
        return Result.success(pageBean);
    }

    @RequestMapping("/delDanmu")
    public Result DelBarrage(@NotNull Integer barrageId) {
        videoBarrageService.DeleteBarrage(GetTokenUserInfo().getUserId(), barrageId);
        return Result.success(null);
    }
}
