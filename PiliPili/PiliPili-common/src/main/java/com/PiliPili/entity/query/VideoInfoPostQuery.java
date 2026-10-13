package com.PiliPili.entity.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoInfoPostQuery {
    private String videoId; // 视频id

    private String videoCover; // 视频封面

    private String videoName; // 视频名称

    private String introduction; // 视频简介

    private String userId; // 用户id

    private LocalDateTime createTime; // 上传时间

    private LocalDateTime lastUpdateTime; // 最后修改时间

    private Integer pCategoryId; // 父级分类id

    private Integer categoryId; // 分类id

    private Integer status; // 0:转码中 1:转码失败 2:待审核 3:审核成功 4:审核失败

    private Integer postType; // 0:自制 1:转载

    private String originInfo; // 搜索说明

    private String tags; // 标签

    private String interaction; // 互动

    private Integer duration; // 视频时长

    private boolean queryUserInfo;

    // 模糊查询条件
    private String videoNameFuzzy;
    private String categoryNameLike;

    // 范围查询
    private Integer sortMin;
    private Integer sortMax;

    // 排序设置
    private String orderByField;
    private boolean ascending = true;

    // 分页参数
    private Integer pageNumber = 1;    // 当前页码（默认1）
    private Integer pageSize = 10;     // 每页数量（默认10）

    private Integer[] excludeStatus; //状态排除
    private boolean queryCount;
}
