package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum MessageEnum {

    LIKE(0, "点赞"),
    SYSTEM(1, "系统"),
    COLLECTION(2, "收藏"),
    COMMENT(3, "评论");

    private int type;
    private String description;

    MessageEnum(int type, String description) {
        this.type = type;
        this.description = description;
    }
}
