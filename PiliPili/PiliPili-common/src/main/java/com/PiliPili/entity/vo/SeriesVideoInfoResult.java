package com.PiliPili.entity.vo;

import com.PiliPili.entity.pojo.SeriesVideo;
import com.PiliPili.entity.pojo.UserVideoSeries;
import lombok.Data;

import java.util.List;

@Data
public class SeriesVideoInfoResult {
    //合集信息
    private UserVideoSeries userVideoSeries;

    //合集的视频信息
    private List<SeriesVideo> seriesVideoList;

}
