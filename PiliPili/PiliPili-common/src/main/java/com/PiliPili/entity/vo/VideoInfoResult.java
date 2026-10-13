package com.PiliPili.entity.vo;

import com.PiliPili.entity.pojo.VideoInfo;
import lombok.Data;

import java.util.List;

@Data
public class VideoInfoResult {
    private VideoInfo videoInfo;
    private List userAction;

    public VideoInfoResult(VideoInfo videoInfo, List userAction){
        this.videoInfo = videoInfo;
        this.userAction = userAction;
    }
}
