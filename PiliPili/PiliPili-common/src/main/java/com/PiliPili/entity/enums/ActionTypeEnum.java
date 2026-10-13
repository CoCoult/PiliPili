package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum ActionTypeEnum {
    LIKE_COMMENT(0, "like_count", "点赞评论"),
    HATE_COMMENT(1, "hate_count", "讨厌评论"),
    LIKE_VIDEO(2, "like_count", "点赞视频"),
    COLLECT_VIDEO(3, "collect_count", "收藏视频"),
    COIN_VIDEO(4, "coin_count", "投币视频"),
    VIDEO_COMMENT(5, "comment_count", "视频评论数"),
    VIDEO_BARRAGE(6, "barrage_count", "弹幕数"),
    VIDEO_PLAY(7, "play_count", "视频播放数");

    private Integer type;
    private String field;
    private String description;

    ActionTypeEnum(Integer type, String field, String description) {
        this.type = type;
        this.field = field;
        this.description = description;
    }

    public static ActionTypeEnum GetActionType(Integer type) {
        for (ActionTypeEnum actionTypeEnum: ActionTypeEnum.values()) {
            if (type.equals(actionTypeEnum.getType())){
                return actionTypeEnum;
            }
        }
        return null;
    }
}
