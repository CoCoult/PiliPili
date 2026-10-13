package com.PiliPili.entity.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentQuery {
    // 精确查询条件
    private Integer commentId;       // 评论ID
    private Integer pCommentId;     // 父级评论ID
    private String videoId;          // 视频ID（长度10）
    private String videoUserId;      // 视频用户ID（长度20）
    private String content;          // 回复内容（长度500）
    private String imgPath;          // 图片路径（长度150）
    private String userId;           // 用户ID（长度20）
    private String replyUserId;      // 回复用户ID（长度20）
    private Integer topType;         // 置顶类型（0:未置顶, 1:置顶）
    private LocalDateTime postTime;  // 发布时间
    private Integer likeCount;       // 点赞数
    private Integer hateCount;       // 讨厌数
    private boolean getChildren; //是否获取子评论

    private boolean queryVideoInfo;

    private String videoNameFuzzy;

    // 范围查询条件
    private LocalDateTime postTimeStart; // 发布时间范围-开始
    private LocalDateTime postTimeEnd;   // 发布时间范围-结束
    private Integer likeCountMin;    // 最小点赞数
    private Integer likeCountMax;    // 最大点赞数
    private Integer hateCountMin;    // 最小讨厌数
    private Integer hateCountMax;    // 最大讨厌数

    // 模糊查询条件
    private String contentLike;      // 内容模糊匹配
    private String videoIdLike;      // 视频ID模糊匹配
    private String userIdLike;       // 用户ID模糊匹配

    // 排序设置
    private String orderByField;     // 排序字段（如"post_time"）
    private boolean ascending = true; // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;  // 当前页码（默认1）
    private Integer pageSize = 10;   // 每页数量（默认10）
}
