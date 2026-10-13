package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.annotation.RecordUserMessage;
import com.PiliPili.entity.enums.MessageEnum;
import com.PiliPili.entity.vo.ActionAndCommentResult;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserAction;
import com.PiliPili.entity.pojo.VideoComment;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.CommentQuery;
import com.PiliPili.entity.query.UserActionQuery;
import com.PiliPili.service.UserActionService;
import com.PiliPili.service.VideoCommentService;
import com.PiliPili.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/comment")
@Validated
@Slf4j
public class VideoCommentController extends BaseController {

    @Resource
    VideoCommentService videoCommentService;

    @Resource
    UserActionService userActionService;

    @Resource
    VideoInfoService videoInfoService;

    /**
     * 发布评论
     * @param videoId
     * @param content
     * @param imgPath
     * @param replyCommentId
     * @return
     */
    @RequestMapping("/postComment")
    @GlobalInterceptor(checkLogin = true)
    @RecordUserMessage(messageType = MessageEnum.COMMENT)
    public Result PostComment(@NotEmpty String videoId,
                              @NotEmpty @Size(max = 500) String content,
                              @Size(max = 100) String imgPath,
                              Integer replyCommentId){
        VideoComment videoComment = new VideoComment();
        videoComment.setVideoId(videoId);
        videoComment.setContent(content);
        videoComment.setImgPath(imgPath);
        videoComment.setUserId(GetTokenUserInfo().getUserId());
        videoComment.setUserName(GetTokenUserInfo().getUserName());
        videoComment.setAvatar(GetTokenUserInfo().getAvatar());
        this.videoCommentService.SaveComment(videoComment, replyCommentId);
        return Result.success(videoComment);
    }

    /**
     * 获取视频评论以及用户操作
     * @param videoId
     * @param pageNum
     * @param orderType
     * @return
     */
    @RequestMapping("/getComment")
    public Result GetComment(@NotEmpty String videoId, Integer pageNum, Integer orderType){

        VideoInfo videoInfo = this.videoInfoService.GetVideoInfoByVideoId(videoId);
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())){
            return Result.success(new ArrayList<>());
        }
        CommentQuery commentQuery = new CommentQuery();
        if (pageNum != null){
            commentQuery.setPageNumber(pageNum);
        }
        System.out.println(pageNum);
        if (pageNum == null){
            System.out.println("空的");
        }
        String field = orderType == null || orderType == 0 ? "like_count desc, comment_id" : "comment_id";
        commentQuery.setOrderByField(field);
        commentQuery.setVideoId(videoId);
        commentQuery.setPCommentId(0);
        commentQuery.setGetChildren(true);
        //获取评论,分页,置顶评论
        PageBean videoCommentList = this.videoCommentService.GetComment(commentQuery, pageNum);
        //刷选出指定评论放到数组顶部
        videoCommentList.getRows();
        //获取当前用户的评论操作
        List<UserAction> userActionList = new ArrayList<>();
        //用户已登录
        if (GetTokenUserInfo().getUserId() != null){
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setVideoId(videoId);
            userActionQuery.setUserId(GetTokenUserInfo().getUserId());
            userActionQuery.setActionType(new Integer[] {ActionTypeEnum.LIKE_COMMENT.getType(), ActionTypeEnum.HATE_COMMENT.getType()});
            userActionList = this.userActionService.GetUserActionByVideoId(userActionQuery);
        }
        ActionAndCommentResult result = new ActionAndCommentResult();
        result.setCommentList(videoCommentList);
        result.setUserActionList(userActionList);
        return Result.success(result);
    }

    /**
     * 置顶评论
     * @param commentId
     * @return
     */
    @RequestMapping("/topComment")
    @GlobalInterceptor(checkLogin = true)
    public Result TopComment(@NotNull Integer commentId){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.videoCommentService.TopComment(commentId, tokenUserInfoDto.getUserId());
        return Result.success();
    }

    /**
     * 取消置顶评论
     * @param commentId
     * @return
     */
    @RequestMapping("/cancelTopComment")
    @GlobalInterceptor(checkLogin = true)
    public Result CancelTopComment(@NotNull Integer commentId){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.videoCommentService.CancelTopComment(commentId, tokenUserInfoDto.getUserId());
        return Result.success();
    }
    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @RequestMapping("/deleteComment")
    @GlobalInterceptor(checkLogin = true)
    public Result DeleteComment(@NotNull Integer commentId){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        this.videoCommentService.DeleteComment(commentId, tokenUserInfoDto.getUserId());
        return Result.success();
    }


}
