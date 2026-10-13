package com.PiliPili.entity.dto;

import com.PiliPili.entity.enums.MessageEnum;
import lombok.Data;

@Data
public class UserMessageDto {

    private String MessageContents; //评论内容

    private String messageContentReply;

    private Integer auditStatus; //审核状态

}
