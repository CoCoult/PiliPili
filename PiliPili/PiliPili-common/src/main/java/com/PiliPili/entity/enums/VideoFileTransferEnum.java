package com.PiliPili.entity.enums;

import lombok.Getter;

@Getter
public enum VideoFileTransferEnum {
    TRANSFERRING(0, "转码中"),
    TRANSFER_FAIL(1, "转码失败"),
    TRANSFER_SUCCESS(2, "转码成功");


    private int code;
    private String description;

    VideoFileTransferEnum(int code, String description){
        this.code = code;
        this.description = description;
    }

}
