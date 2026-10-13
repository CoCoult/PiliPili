package com.PiliPili.entity.dto;

import lombok.Getter;

@Getter
public class VideoPlayInfoDto {
    private String videoId;
    private String userId;
    private Integer fileIndex;

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFileIndex(Integer fileIndex) {
        this.fileIndex = fileIndex;
    }
}
