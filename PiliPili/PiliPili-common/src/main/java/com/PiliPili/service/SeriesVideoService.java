package com.PiliPili.service;

import com.PiliPili.entity.pojo.SeriesVideo;
import com.PiliPili.entity.query.SeriesVideoQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SeriesVideoService {

    /**
     * 获取合集视频列表
     * @param seriesVideoQuery
     * @return
     */
    List<SeriesVideo> GetVideoListBySeriesId(SeriesVideoQuery seriesVideoQuery);

}
