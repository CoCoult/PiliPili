package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserAction {
    private Integer actionId; //用户行为id 自增

    private String videoId; //视频id

    private String videoUserId; //视频所属用户id

    private Integer commentId; //评论id

    private Integer actionType; //0:点赞评论1：讨厌评论2：点赞视频3：收藏视频4：投币视频

    private Integer actionCount; //投币数

    private String userId; //用户id

    private LocalDateTime actionTime; //操作时间

    //收藏视频
    private String videoName; //视频名

    private String videoCover; //视频封面
}
