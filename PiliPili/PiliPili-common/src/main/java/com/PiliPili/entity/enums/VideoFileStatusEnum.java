package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum VideoFileStatusEnum {
    NOT_UPDATE(0, "未更新"),
    UPDATE(1, "更新");

    private int code;
    private String description;

    VideoFileStatusEnum(int code, String description){
        this.code = code;
        this.description = description;
    }
}
