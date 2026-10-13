package com.PiliPili.entity.query;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMessageQuery {
    private Integer messageId;
    private String userId;
    private String videoId;
    private Integer messageType;
    private String sendUserId;
    private Integer readType;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private String extendJsonContains;

    // 分页参数
    private Integer pageNum = 1;
    private Integer pageSize = 20;

    // 排序参数
    private String orderByField; //排序参数
    private String order = "ASC"; // 默认升序
}
