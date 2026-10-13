package com.PiliPili.entity.query;

import lombok.Data;

@Data
public class SeriesVideoQuery {
    private Integer seriesId;

    private String videoId;

    private String userId;

    private Integer sort;

    //是否查询视频信息
    private boolean selectVideoInfo;
    //
    // 排序设置
    private String orderByField;     // 排序字段（如"post_time"）
    private boolean ascending = true; // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;  // 当前页码（默认1）
    private Integer pageSize = 10;   // 每页数量（默认10）
}
