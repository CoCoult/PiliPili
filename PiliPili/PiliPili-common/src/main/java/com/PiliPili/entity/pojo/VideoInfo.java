package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfo {
    private String videoId; // 视频id

    private String videoCover; // 视频封面

    private String videoName; // 视频名称

    private String introduction; // 视频简介

    private String userId; // 用户id


    private LocalDateTime createTime; // 上传时间

    private LocalDateTime lastUpdateTime; // 最后修改时间

    private Integer pCategoryId; // 父级分类id

    private Integer categoryId; // 分类id

    private Integer postType; // 0:自制 1:转载

    private String originInfo; // 搜索说明

    private String tags; // 标签

    private String interaction; // 互动

    private Integer duration; // 视频时长

    private Integer playCount; // 播放数

    private Integer likeCount; // 点赞数

    private Integer collectCount; // 收藏数

    private Integer barrageCount; // 弹幕数

    private Integer commentCount; // 评论数

    private Integer coinCount; // 硬币数

    private Integer recommendType; // 0未推荐1推荐

    private LocalDateTime lastPlayTime; // 最后观看时间

    private String userName; //用户姓名

    private String avatar; //用户头像

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    public void setBarrageCount(Integer barrageCount) {
        this.barrageCount = barrageCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public void setCoinCount(Integer coinCount) {
        this.coinCount = coinCount;
    }
}
