package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFocus {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 关注的用户id
     */
    private String focusUserId;

    /**
     * 关注时间
     */
    private LocalDateTime focusTime;

    /**
     * 是否互关
     */
    private Integer focusType;

    private String userName;

    private String userAvatar;

    private String selfIntroduction;
}
