package com.PiliPili.service;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoComment;
import com.PiliPili.entity.query.CommentQuery;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.xml.stream.events.Comment;

@Service
public interface VideoCommentService {
    /**
     * 发布评论
     */
    void SaveComment(VideoComment videoComment, Integer replayComment);

    /**
     * 获取当前视频的所有评论并排序
     * @param commentQuery
     * @return
     */
    PageBean GetComment(CommentQuery commentQuery, Integer pageNum);

    /**
     * 置顶评论
     * @param commentId
     * @param userId
     */
    void TopComment(Integer commentId, String userId);

    /**
     * 取消置顶评论
     * @param commentId
     * @param userId
     */
    void CancelTopComment(Integer commentId, String userId);

    /**
     * 删除评论
     * @param commentId
     * @param userId
     */
    void DeleteComment(Integer commentId, String userId);

    /**
     * 获取当前用户发布视频的评论
     * @param commentQuery
     * @return
     */
    PageBean GetCommentByPage(CommentQuery commentQuery);

}
