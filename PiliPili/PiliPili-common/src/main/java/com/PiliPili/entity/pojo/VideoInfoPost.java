package com.PiliPili.entity.pojo;

import com.PiliPili.entity.enums.VideoStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoPost {

    @Getter
    private String videoId; // 视频id

    @Getter
    private String videoCover; // 视频封面

    @Getter
    private String videoName; // 视频名称

    @Getter
    private String introduction; // 视频简介

    @Getter
    private String userId; // 用户id

    @Getter
    private LocalDateTime createTime; // 上传时间

    @Getter
    private LocalDateTime lastUpdateTime; // 最后修改时间

    @Getter
    private Integer pCategoryId; // 父级分类id

    @Getter
    private Integer categoryId; // 分类id

    @Getter
    private Integer status; // 0:转码中 1:转码失败 2:待审核 3:审核成功 4:审核失败

    @Getter
    private Integer postType; // 0:自制 1:转载

    @Getter
    private String originInfo; // 搜索说明

    @Getter
    private String tags; // 标签

    @Getter
    private String interaction; // 互动e

    @Getter
    private Integer duration; // 视频时长

    private String statusName = getStatusName();


    public String getStatusName() {
        return statusName == null ? "" : VideoStatusEnum.getDescriptionByCode(status);
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setpCategoryId(Integer pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setPostType(Integer postType) {
        this.postType = postType;
    }

    public void setOriginInfo(String originInfo) {
        this.originInfo = originInfo;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setInteraction(String interaction) {
        this.interaction = interaction;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
