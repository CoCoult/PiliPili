package com.PiliPili.entity.enums;

public enum BarrageModeEnum {
    TOP(0, "顶部"),
    BOTTOM(1, "底部"),
    SLIDE(2, "滑动弹幕");

    private int code;
    private String description;

    BarrageModeEnum(int code, String description){
        this.code = code;
        this.description = description;
    }
}
