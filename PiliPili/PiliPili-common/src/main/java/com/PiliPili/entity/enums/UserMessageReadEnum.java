package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum UserMessageReadEnum {
    NOT_READ(0, "未读"),
    READ(1, "已读");

    private int code;
    private String description;

    UserMessageReadEnum(int code, String description){
        this.code = code;
        this.description = description;
    }
}
