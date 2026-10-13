package com.PiliPili.entity.dto;

import com.PiliPili.entity.pojo.VideoInfoPost;
import lombok.Data;

@Data
public class VideoInfoPostDTO extends VideoInfoPost {
    private Integer playCount;      // c.play_count
    private Integer likeCount;     // c.like_count
    private Integer barrageCount;  // c.barrage_count
    private Integer commentCount;  // c.comment_count
    private Integer coinCount;     // c.coin_count
    private Integer collectCount;  // c.collect_count
    private Integer recommendType; // c.recommend_type
    private String userName;       // u.user_name
    private String avatar;         // u.avatar

    // Getters and Setters
}
