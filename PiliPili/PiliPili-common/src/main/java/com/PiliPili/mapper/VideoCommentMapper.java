package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.VideoComment;
import com.PiliPili.entity.query.CommentQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoCommentMapper {
    /**
     * 根据评论id获取评论
     */
    VideoComment GetCommentByCommentId(Integer commentId);

    /**
     * 保存评论
     */
    void InsertComment(VideoComment videoComment);

    /**
     * 获取视频评论
     * @param commentQuery
     * @return
     */
    List<VideoComment> GetCommentByVideoId(CommentQuery commentQuery);

    /**
     * 获取视频评论以及子评论
     * @param commentQuery
     * @return
     */
    List<VideoComment> GetCommentWithChildren(CommentQuery commentQuery);

    /**
     * 更新评论属性的数量（点赞数等）
     * @param commentId
     * @param field
     * @param changeCount
     * @param opposeField
     * @param opposeChangeCount
     */
    void UpdateCommentCount(Integer commentId, String field, Integer changeCount, String opposeField, Integer opposeChangeCount);

    /**
     * 更新评论信息
     * @param commentQuery
     */
    void UpdateComment(CommentQuery commentQuery);

    /**
     * 根据评论id更新评论信息
     * @param commentQuery
     */
    void UpdateCommentByCommentId(CommentQuery commentQuery);

    /**
     * 删除评论
     * @param commentId
     */
    void DeleteComment(Integer commentId);

    /**
     * 删除二级评论
     * @param commentId
     */
    void DeleteCommentByPId(Integer commentId, String videoId);

    /**
     * 删除视频所有评论
     * @param videoId
     */
    void DeleteCommentByVideoId(@Param("videoId") String videoId);
}
