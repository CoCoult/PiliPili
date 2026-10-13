package com.PiliPili.service.impl;

import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.enums.CommentTopTypeEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.pojo.VideoComment;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.CommentQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.UserMapper;
import com.PiliPili.mapper.VideoCommentMapper;
import com.PiliPili.mapper.VideoInfoMapper;
import com.PiliPili.service.VideoCommentService;
import com.PiliPili.service.VideoInfoService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoCommentServiceImpl implements VideoCommentService {

    @Resource
    VideoInfoMapper videoInfoMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    VideoCommentMapper videoCommentMapper;

    /**
     * 发布评论
     *
     * @param videoComment
     * @param replyCommentId
     */
    @Override
    public void SaveComment(VideoComment videoComment, Integer replyCommentId) {
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(600, "参数错误");
        }
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
            throw new BusinessException("UP主评论区已关闭");
        }
        if (replyCommentId != null) {
            //获取回复人的评论信息
            VideoComment replyComment = this.videoCommentMapper.GetCommentByCommentId(replyCommentId);
            if (replyComment == null || !replyComment.getVideoId().equals(videoComment.getVideoId())) {
                throw new BusinessException(600, "参数错误");
            }
            //回复评论为一级评论
            if (replyComment.getPCommentId() == 0) {
                videoComment.setPCommentId(replyComment.getCommentId());
            }
            //回复评论为二级评论
            else {
                videoComment.setPCommentId(replyComment.getPCommentId());
                //保存回复人的信息
                videoComment.setReplyUserId(replyComment.getUserId());
            }
            //保存回复人的姓名
            User user = this.userMapper.GetUserInfoByUserId(replyComment.getUserId());
            videoComment.setReplyUserName(user.getUserName());
        } else {
            //若评论是一级评论
            videoComment.setPCommentId(0);
        }
        videoComment.setPostTime(LocalDateTime.now());
        videoComment.setVideoUserId(videoInfo.getUserId());
        this.videoCommentMapper.InsertComment(videoComment);

        //更新视频评论数（只记录一级评论）
        if (videoComment.getPCommentId() == 0) {
            this.videoInfoMapper.UpdateVideoCount(videoComment.getVideoId(), Constants.ONE, "comment_count");
        }
    }

    /**
     * 获取当前视频的所有评论并排序
     *
     * @param commentQuery
     * @return
     */
    @Override
    public PageBean GetComment(CommentQuery commentQuery, Integer pageNum) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(commentQuery.getPageNumber(), commentQuery.getPageSize());
        List<VideoComment> videoCommentList = new ArrayList<>();
        if (commentQuery.isGetChildren()) {
            videoCommentList = this.videoCommentMapper.GetCommentWithChildren(commentQuery);
        } else {
            videoCommentList = this.videoCommentMapper.GetCommentByVideoId(commentQuery);
        }
        //获取置顶评论
        if (pageNum == 1) {
            List<VideoComment> topComment = GetTopComment(commentQuery.getVideoId());
            if (!topComment.isEmpty()) {
                //过滤非置顶评论
                List<VideoComment> commentList =
                        videoCommentList.stream().filter(item -> !item.getCommentId().equals(topComment.get(0).getCommentId())).collect(Collectors.toList());
                //将置顶评论放置列表首部
                commentList.addAll(0, topComment);
                videoCommentList = commentList;
            }
        }
        PageInfo<VideoComment> videoCommentPageInfo = new PageInfo<>(videoCommentList);
        pageBean.setPageNum(commentQuery.getPageNumber());
        pageBean.setPageSize(commentQuery.getPageSize());
        pageBean.setTotal(videoCommentPageInfo.getTotal());
        pageBean.setRows(videoCommentPageInfo.getList());
        pageBean.setPageTotal(videoCommentPageInfo.getPages());
        return pageBean;
    }

    /**
     * 找出置顶评论
     */
    public List<VideoComment> GetTopComment(String videoId) {
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setVideoId(videoId);
        commentQuery.setGetChildren(true);
        commentQuery.setPCommentId(0);
        commentQuery.setTopType(CommentTopTypeEnum.TOP.getCode());
        List<VideoComment> topComment = this.videoCommentMapper.GetCommentWithChildren(commentQuery);
        return topComment;
    }

    /**
     * 置顶评论
     *
     * @param commentId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void TopComment(Integer commentId, String userId) {
        this.CancelTopComment(commentId, userId);
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setCommentId(commentId);
        commentQuery.setTopType(CommentTopTypeEnum.TOP.getCode());

        this.videoCommentMapper.UpdateCommentByCommentId(commentQuery);

    }

    /**
     * 取消置顶评论
     *
     * @param commentId
     * @param userId
     */
    @Override
    public void CancelTopComment(Integer commentId, String userId) {
        VideoComment videoComment = new VideoComment();
        videoComment = this.videoCommentMapper.GetCommentByCommentId(commentId);
        if (videoComment == null) {
            throw new BusinessException(600, "参数错误");
        }
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoComment.getVideoId());
        if (videoInfo == null){
            throw new BusinessException(600, "参数错误");
        }
//        System.out.println(videoComment.getVideoId());
//        System.out.println(userId);
//        System.out.println(videoInfo.getUserId());
        if (!userId.equals(videoInfo.getUserId())){
            throw new BusinessException(600, "无权限");
        }
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setTopType(CommentTopTypeEnum.NOT_TOP.getCode());
        commentQuery.setCommentId(commentId);
        commentQuery.setVideoId(videoComment.getVideoId());
        this.videoCommentMapper.UpdateComment(commentQuery);
    }

    /**
     * 删除评论
     *
     * @param commentId
     * @param userId
     */
    @Override
    public void DeleteComment(Integer commentId, String userId) {
        VideoComment videoComment = this.videoCommentMapper.GetCommentByCommentId(commentId);
        if (videoComment == null) {
            throw new BusinessException(600, "参数错误");
        }
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(600, "视频不存在");
        }
        //仅视频发布者或评论发布者可删除评论
        if (userId != null && !videoInfo.getUserId().equals(userId) && !videoComment.getUserId().equals(userId)){
            throw new BusinessException(600, "无权限");
        }
        this.videoCommentMapper.DeleteComment(commentId);
        //更新视频评论并删除二级评论
        if (videoComment.getPCommentId() == 0) {
            Integer changeCount = -Constants.ONE;
            this.videoInfoMapper.UpdateVideoCount(videoInfo.getVideoId(), changeCount, ActionTypeEnum.VIDEO_COMMENT.getField());
            this.videoCommentMapper.DeleteCommentByPId(videoComment.getCommentId(), videoInfo.getVideoId());
        }
    }

    /**
     * 获取当前用户发布视频的评论
     *
     * @param commentQuery
     * @return
     */
    @Override
    public PageBean GetCommentByPage(CommentQuery commentQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(commentQuery.getPageNumber(), commentQuery.getPageSize());
        Page<VideoComment> videoCommentPage = (Page<VideoComment>) this.videoCommentMapper.GetCommentByVideoId(commentQuery);
        pageBean.setPageSize(commentQuery.getPageSize());
        pageBean.setPageNum(commentQuery.getPageNumber());
        pageBean.setPageTotal(videoCommentPage.getPages());
        pageBean.setRows(videoCommentPage.getResult());
        pageBean.setTotal(videoCommentPage.getTotal());
        return pageBean;
    }
}
