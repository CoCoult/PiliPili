package com.PiliPili.entity.vo;

import lombok.Data;

@Data
public class UserVideoCountResult {
    private Integer auditPassCount;
    private Integer failPassCount;
    private Integer inProgressCount;
}
