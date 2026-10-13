package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoComment {
    private Integer commentId; //评论id

    private Integer pCommentId; //父级评论id

    private String videoId; //视频id

    private String videoUserId; //视屏所属用户id

    private String content; //回复内容

    private String imgPath; //回复图片存储路径

    private String userId; //评论用户id

    private String replyUserId; //回复评论的用户id

    private Integer topType; //0未置顶 1置顶

    private LocalDateTime postTime; //评论发布时间

    private Integer likeCount; //点赞数

    private Integer hateCount; //讨厌数



    // 关联查询的字段
    private String videoName;

    private String videoCover;

    private String avatar; //用户头像

    private String userName; //用户名

    private String replyUserName; //回复人的姓名

    private String replyAvatar;

    private List<VideoComment> children = new ArrayList<>();//子评论

}
