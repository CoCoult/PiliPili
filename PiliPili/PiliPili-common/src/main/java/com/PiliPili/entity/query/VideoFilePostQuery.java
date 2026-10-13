package com.PiliPili.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoFilePostQuery {

    private String fileId; //唯一id

    private String uploadId; //上传id

    private String userId; //用户Id

    private String videoId; //视频id


    // 模糊查询条件
    private String categoryCodeLike;
    private String categoryNameLike;

    // 范围查询
    private Integer sortMin;
    private Integer sortMax;

    // 排序设置
    private String orderByField;
    private boolean ascending = true;
}
