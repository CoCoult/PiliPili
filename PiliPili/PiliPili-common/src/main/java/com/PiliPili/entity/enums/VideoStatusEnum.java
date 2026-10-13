package com.PiliPili.entity.enums;

import com.PiliPili.exception.BusinessException;
import lombok.Getter;

@Getter
public enum VideoStatusEnum {
    TRANSCODING(0, "转码中"),
    TRANSCODE_FAILED(1, "转码失败"),
    PENDING_REVIEW(2, "待审核"),
    APPROVED(3, "审核成功"),
    REJECTED(4, "审核失败");

    // Getter 方法
    private final int code;
    private final String description;

    VideoStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // 根据 code 获取对应枚举
    public static VideoStatusEnum fromCode(int code) {
        for (VideoStatusEnum status : VideoStatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new BusinessException("无效的状态码: " + code);
    }

    // 根据 code 获取中文描述
    public static String getDescriptionByCode(int code) {
        return fromCode(code).description;
    }

}
