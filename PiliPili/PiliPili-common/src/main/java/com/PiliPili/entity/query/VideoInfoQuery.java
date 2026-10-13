package com.PiliPili.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoQuery {
    // 精确查询条件
    private String videoId;          // 视频ID
    private String videoCover;        // 视频封面
    private String videoName;         // 视频名称
    private String introduction;      // 视频简介
    private String userId;            // 用户ID
    private Integer pCategoryId;      // 父级分类ID
    private Integer categoryId;       // 分类ID
    private Integer postType;         // 发布类型（0:自制, 1:转载）
    private String originInfo;        // 源资源说明
    private String tags;              // 标签
    private String interaction;       // 互动
    private Integer duration;         // 视频时长（秒）
    private boolean getUserInfo;      //是否查询用户信息
    private Integer recommendType;   //0不推荐，1推荐
    private Integer createTime;
    private Integer categoryIdOrPCategoryId; //一级分类或子分类

    private boolean openBarrage; //开启弹幕

    private String[] videoIdArray; //视频id数组

    private String[] excludeVideoId; //需要排除的视频id

    @Getter
    private Integer lastPlayHour;

    public void setLastPlayHour(Integer lastPlayHour) {
        this.lastPlayHour = lastPlayHour;
    }

    // 范围查询条件
    private LocalDateTime createTimeStart;    // 上传时间范围-开始
    private LocalDateTime createTimeEnd;      // 上传时间范围-结束
    private LocalDateTime updateTimeStart;    // 最后修改时间范围-开始
    private LocalDateTime updateTimeEnd;      // 最后修改时间范围-结束
    private Integer minPlayCount;      // 最小播放量
    private Integer maxPlayCount;      // 最大播放量
    private Integer minLikeCount;      // 最小点赞数
    private Integer maxLikeCount;      // 最大点赞数
    private Integer minBarrageCount;   // 最小弹幕数
    private Integer maxBarrageCount;   // 最大弹幕数
    private Integer minCommentCount;   // 最小评论数
    private Integer maxCommentCount;   // 最大评论数

    // 模糊查询条件
    private String videoNameLike;      // 视频名称模糊匹配
    private String introductionLike;   // 简介模糊匹配
    private String tagsLike;           // 标签模糊匹配
    private String interactionLike;    // 互动内容模糊匹配

    // 排序设置
    private String orderByField;       // 排序字段（如"create_time"）
    private boolean ascending = true;  // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;    // 当前页码（默认1）
    private Integer pageSize = 10;     // 每页数量（默认10）


}
