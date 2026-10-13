package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum VideoOrderTypeEnum {
    CREATE_TIME(0, "create_time", "创建时间"),
    HATE_COMMENT(1, "hate_count", "讨厌评论"),
    LIKE_VIDEO(2, "like_count", "点赞视频"),
    COLLECT_VIDEO(3, "collect_count", "收藏视频"),
    COIN_VIDEO(4, "coin_count", "投币视频"),
    VIDEO_COMMENT(5, "comment_count", "视频评论数"),
    VIDEO_BARRAGE(6, "barrage_count", "弹幕数"),
    VIDEO_PLAY(7, "play_count", "视频播放数");

    private int type;
    private String field;
    private String description;

    VideoOrderTypeEnum(Integer type, String field, String description) {
        this.type = type;
        this.field = field;
        this.description = description;
    }

    public static VideoOrderTypeEnum GetVideoOrderType(Integer type) {
        for (VideoOrderTypeEnum videoOrderTypeEnum: VideoOrderTypeEnum.values()) {
            if (type.equals(videoOrderTypeEnum.getType())){
                return videoOrderTypeEnum;
            }
        }
        return null;
    }
}
