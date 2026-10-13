package com.PiliPili.entity.dto;

import lombok.Data;

@Data
public class UserMessageCountDto {
    private Integer messageType; //消息类型

    private Integer messageCount; //消息数量

}
