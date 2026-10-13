package com.PiliPili.entity.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVideoSeriesQuery {
    private Integer seriesId; //合集id

    private String seriesName;

    private String seriesDescription;

    private String userId;

    private Integer sort;

    private LocalDateTime updateTime;

    // 排序设置
    private String orderByField;    // 排序字段（如"post_time"）

    // 分页参数
    private Integer pageNumber = 1; // 当前页码（默认1）
    private Integer pageSize = 10;  // 每页数量（默认10）
}
