package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeriesVideo {
    private Integer seriesId;

    private String videoId;

    private String userId;

    private Integer sort;

    //视频信息
    private String videoName; //视屏名称

    private String videoCover; //视频封面

    private Integer playCount; //播放数

    private LocalDateTime createTime; //发布时间
}
