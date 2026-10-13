package com.PiliPili.entity.vo;

import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoInfoPost;
import lombok.Data;

import java.util.List;

@Data
public class VideoInfoListResult {
    private VideoInfoPost videoInfo;
    private List<VideoInfoFilePost> videoInfoFilePostList;
}
