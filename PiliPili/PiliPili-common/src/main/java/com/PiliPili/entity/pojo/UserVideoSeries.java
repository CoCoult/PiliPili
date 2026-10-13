package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVideoSeries {
    private Integer seriesId; //合集id

    private String seriesName;

    private String seriesDescription;

    private String userId;

    private Integer sort;

    private LocalDateTime updateTime;

    //
    private String videoName; //视频名称

    private String videoCover; //视频封面

    private List<VideoInfo> videoInfoList; //合集前五个视频
}
