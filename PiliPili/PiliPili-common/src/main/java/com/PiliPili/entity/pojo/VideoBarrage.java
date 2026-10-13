package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoBarrage {

    private Integer barrageId; // 弹幕Id（自增主键）

    private String videoId; // 视频id

    private String fileId; // 分pid

    private String userId; // 用户id

    private LocalDateTime postTime; // 发布时间

    private String text; // 内容

    private Integer mode; // 弹幕展示位置（0:滚动 1:顶部 2:底部等）

    private String color; // 颜色（如"#FF0000"）

    private Integer time; // 弹幕展示时间（单位：秒）

    private String videoName;

    private String videoCover;

    private String userName;
}

