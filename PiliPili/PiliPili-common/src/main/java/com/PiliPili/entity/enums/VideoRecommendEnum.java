package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum VideoRecommendEnum {
    NOT_RECOMMEND(0, "未推荐"),
    RECOMMEND(1, "推荐");

    private int code;
    private String description;

    VideoRecommendEnum(int code, String description){
        this.code = code;
        this.description = description;
    }
}
