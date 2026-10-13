package com.PiliPili.entity.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoPlayHistory {

    private String userId;

    private String videoId;

    private Integer fileIndex;

    private LocalDateTime lastUpdateTime;

    //
    private String videoName; //视频名

    private String videoCover; //视频封面

}
