package com.PiliPili.entity.query;

import lombok.Data;

@Data
public class VideoInfoFileQuery {
    private String fileId; //唯一id

    private String videoId; //视频id

    private String userId; //用户Id

    private Integer fileIndex; //文件索引

    private String fileName; //文件名

    private Long fileSize; //文件大小

    private String filePath; //文件路径

    private Integer duration; //视频时长

    // 排序设置
    private String orderByField;       // 排序字段（如"create_time"）
    private boolean ascending = true;  // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;    // 当前页码（默认1）
    private Integer pageSize = 10;     // 每页数量（默认10）
}
