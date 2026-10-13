package com.PiliPili.service.impl;

import com.PiliPili.entity.pojo.SeriesVideo;
import com.PiliPili.entity.query.SeriesVideoQuery;
import com.PiliPili.mapper.SeriesVideoMapper;
import com.PiliPili.service.SeriesVideoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SeriesVideoServiceImpl implements SeriesVideoService {

    @Resource
    SeriesVideoMapper seriesVideoMapper;

    /**
     * 获取合集视频列表
     *
     * @param seriesVideoQuery
     * @return
     */
    @Override
    public List<SeriesVideo> GetVideoListBySeriesId(SeriesVideoQuery seriesVideoQuery) {
        return this.seriesVideoMapper.GetVideoListBySeriesId(seriesVideoQuery);
    }

}
