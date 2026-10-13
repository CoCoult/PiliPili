package com.PiliPili.entity.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum StatisticsTypeEnum {
    PLAY(0, "播放量"),
    USER(1, "粉丝"),
    LIKE(2, "点赞"),
    COLLECTION(3, "收藏"),
    COIN(4, "投币"),
    COMMENT(5, "评论"),
    BARRAGE(6, "弹幕");

    private Integer type;
    private String description;

    StatisticsTypeEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }
}
