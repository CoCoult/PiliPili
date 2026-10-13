package com.PiliPili.entity.dto;

import lombok.Data;

@Data
public class UserCountDto {
    private Integer fansCount; //粉丝数

    private Integer focusCount; //关注数

    private Integer currentCoinCount; //当前用户硬币数

}
