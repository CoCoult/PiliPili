package com.PiliPili.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysSettingDto implements Serializable {
    private Integer registerCoinCount = 10; //注册获得硬币数
    private Integer postVideoCoinCount = 10; //发布视频获得硬币数
    private Integer videoSize = 100; //限制视频大小（MB）
    private Integer videoPCount = 5; //视频分p数
    private Integer videoCount = 10;
    private Integer commentCount = 20; //评论数
    private Integer barrageCount = 20; //弹幕数

}
