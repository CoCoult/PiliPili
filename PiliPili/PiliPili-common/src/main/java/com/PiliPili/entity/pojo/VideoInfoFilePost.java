package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfoFilePost {

    private String fileId; //唯一id

    private String uploadId; //视频id

    private String userId; //用户Id

    private String videoId; //视频id

    private Integer fileIndex; //文件索引

    private String fileName; //文件名

    private Long fileSize; //文件大小

    private String filePath; //文件路径

    private Integer updateType; //0无更新，1有更新

    private Integer transferResult; //0转码中1转码失败2转码成功

    private Integer duration; //视频时长
}
