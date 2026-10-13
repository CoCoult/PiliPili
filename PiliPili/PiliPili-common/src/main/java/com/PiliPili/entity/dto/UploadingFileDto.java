package com.PiliPili.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadingFileDto implements Serializable {
    private String uploadId; //文件上传id
    private String fileName;
    private Integer chunkIndex;
    private Integer chunks; //分片数
    private Long fileSize = 0L;
    private String filePath;
}
