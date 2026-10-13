package com.PiliPili.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoInfoEsDto {
    private String videoId; // 视频id

    private String videoCover; // 视频封面

    private String videoName; // 视频名称

    private String userId; // 用户id

    private LocalDateTime createTime; // 上传时间

    private String tags; // 标签

    private Integer playCount; // 播放数

    private Integer collectCount; // 点赞数

    private Integer barrageCount; // 弹幕数

}
