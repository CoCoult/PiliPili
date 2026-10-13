package com.PiliPili.admin.controller;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.query.CommentQuery;
import com.PiliPili.entity.query.VideoBarrageQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.VideoBarrageService;
import com.PiliPili.service.VideoCommentService;
import org.elasticsearch.cluster.metadata.ItemUsage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/interact")
public class InteractionController {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoBarrageService videoBarrageService;

    /**
     * 获取评论
     * @param pageNum
     * @param videoNameFuzzy
     * @return
     */
    @RequestMapping("/getComment")
    public Result GetComment(Integer pageNum, String videoNameFuzzy) {
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setOrderByField("comment_id desc");
        if (pageNum != null) {
            commentQuery.setPageNumber(pageNum);
        }
        commentQuery.setVideoNameFuzzy(videoNameFuzzy);
        commentQuery.setQueryVideoInfo(true);
        PageBean pageBean = this.videoCommentService.GetCommentByPage(commentQuery);
        return Result.success(pageBean);
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @RequestMapping("/deleteComment")
    public Result DeleteComment(@NotNull Integer commentId) {
        this.videoCommentService.DeleteComment(commentId, null);
        return Result.success();
    }

    /**
     * 获取弹幕
     * @param pageNum
     * @param videoNameFuzzy
     * @return
     */
    @RequestMapping("/getBarrage")
    public Result GetBarrage(Integer pageNum, String videoNameFuzzy) {
        VideoBarrageQuery videoBarrageQuery = new VideoBarrageQuery();
        if(pageNum != null) {
            videoBarrageQuery.setPageNumber(pageNum);
        }
        videoBarrageQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoBarrageQuery.setQueryVideoInfo(true);
        PageBean pageBean = this.videoBarrageService.GetBarrageByParam(videoBarrageQuery);
        return Result.success(pageBean);
    }

    /**
     * 删除评论
     * @param barrageId
     * @return
     */
    @RequestMapping("/deleteBarrage")
    public Result DeleteBarrage(@NotNull Integer barrageId) {
        this.videoBarrageService.DeleteBarrage(null, barrageId);
        return Result.success();
    }
}
