
package com.PiliPili.entity.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoPlayHistoryQuery {

    private String userId;

    private String videoId;

    private Integer fileIndex;

    private LocalDateTime lastUpdateTime;

    private boolean selectVideoInfo;


    // 排序设置
    private String orderByField;       // 排序字段
    private boolean ascending = true;  // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;    // 当前页码（默认1）
    private Integer pageSize = 10;     // 每页数量（默认10）
}
