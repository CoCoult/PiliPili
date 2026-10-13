package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum CommentTopTypeEnum {
    NOT_TOP(0, "未置顶"),
    TOP(1, "置顶");

    private int code;
    private String description;

    CommentTopTypeEnum(int code, String description){
        this.code = code;
        this.description = description;
    }
}
