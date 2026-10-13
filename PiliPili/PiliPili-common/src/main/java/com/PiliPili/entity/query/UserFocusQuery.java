package com.PiliPili.entity.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFocusQuery {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 关注的用户id
     */
    private String focusUserId;

    /**
     * 关注时间
     */
    private LocalDateTime focusTime;

    /**
     * 查询类型
     */
    private Integer queryType;

    // 排序设置
    private String orderByField;     // 排序字段（如"action_time"）
    private boolean ascending = true; // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;  // 当前页码（默认1）
    private Integer pageSize = 10;   // 每页数量（默认10）
}

