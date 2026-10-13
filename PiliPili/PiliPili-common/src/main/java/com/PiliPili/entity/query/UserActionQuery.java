package com.PiliPili.entity.query;

import com.PiliPili.entity.pojo.VideoInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActionQuery {
    // 精确查询条件
    private Integer actionId;        // 自增ID
    private String videoId;          // 视频ID（长度10）
    private String videoUserId;      // 视频用户ID（长度20）
    private Integer commentId;       // 评论ID
    private Integer[] actionType;      // 操作类型（0:点赞评论, 1:讨厌评论, 2:点赞视频, 3:收藏视频, 4:投币视频）
    private Integer actionCount;     // 投币数量
    private String userId;           // 用户ID（长度20）
    private LocalDateTime actionTime; // 操作时间



    private boolean collectVideoInfo;

    // 范围查询条件
    private LocalDateTime actionTimeStart; // 操作时间范围-开始
    private LocalDateTime actionTimeEnd;   // 操作时间范围-结束
    private Integer actionCountMin;  // 最小投币数量
    private Integer actionCountMax;  // 最大投币数量

    // 模糊查询条件
    private String videoIdLike;      // 视频ID模糊匹配
    private String userIdLike;       // 用户ID模糊匹配

    // 排序设置
    private String orderByField;     // 排序字段（如"action_time"）
    private boolean ascending = true; // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;  // 当前页码（默认1）
    private Integer pageSize = 10;   // 每页数量（默认10）
}
