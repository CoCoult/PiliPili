package com.PiliPili.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoBarrageQuery {
    // 精确查询条件
    private Integer barrageId;      // 弹幕ID
    private String videoId;         // 视频ID
    private String fileId;          // 分P ID
    private String userId;          // 用户ID
    private String text;            // 内容
    private Integer mode;           // 弹幕模式（0:滚动 1:顶部 2:底部）
    private String color;           // 颜色（如#FF0000）
    private Integer time;           // 展示时间（秒）
    private String videoUserId;
    private boolean queryVideoInfo = false;

    private String videoNameFuzzy;
    // 范围查询条件
    private LocalDateTime postTimeStart; // 发布时间范围-开始
    private LocalDateTime postTimeEnd;   // 发布时间范围-结束
    private Integer timeMin;        // 最小展示时间
    private Integer timeMax;        // 最大展示时间

    // 模糊查询条件
    private String textLike;        // 内容模糊匹配
    private String userIdLike;      // 用户ID模糊匹配

    // 排序设置
    private String orderByField;    // 排序字段（如"post_time"）
    private boolean ascending = true; // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1; // 当前页码（默认1）
    private Integer pageSize = 10;  // 每页数量（默认10）
}
