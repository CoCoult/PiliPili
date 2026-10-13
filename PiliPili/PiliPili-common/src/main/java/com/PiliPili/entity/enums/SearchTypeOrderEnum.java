package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum SearchTypeOrderEnum {
    VIDEO_PLAY(0, "playCount", "视频播放数"),
    VIDEO_TIME(1, "createTime", "视频创建时间"),
    VIDEO_BARRAGE(2, "barrageCount", "视频弹幕数"),
    VIDEO_COLLECT(3, "collectCount", "视频收藏数");

    private int type;
    private String field;
    private String description;

    SearchTypeOrderEnum(Integer type, String field, String description) {
        this.type = type;
        this.field = field;
        this.description = description;
    }

    public static SearchTypeOrderEnum GetOrderType(Integer type) {
        if (type == null) return null;
        for (SearchTypeOrderEnum searchTypeOrderEnum: SearchTypeOrderEnum.values()) {
            if (type.equals(searchTypeOrderEnum.getType())){
                return searchTypeOrderEnum;
            }
        }
        return null;
    }
}
