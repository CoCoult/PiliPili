package com.PiliPili.entity.pojo;

import com.PiliPili.entity.dto.UserMessageDto;
import com.PiliPili.utils.JsonUtils;
import com.PiliPili.utils.StringToolsUtils;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class UserMessage {

    private Integer messageId;//自增

    private String userId;

    private String videoId;

    private Integer messageType; //消息类型

    private String sendUserId; //发送人id

    private Integer readType; //0未读，1已读

    private LocalDateTime createTime;

    private String extendJson; //扩展信息

    //
    private String userName; //发送消息的用户名

    private String avatar; //发送消息的用户头像

    private String videoName; //视频名

    private String videoCover; //视频封面


    private UserMessageDto userMessageDto;

    public UserMessageDto getUserMessageDto() {
        return ((this.extendJson == null || this.extendJson.isEmpty()) ? new UserMessageDto() : JsonUtils.convertJsonToObj(extendJson, UserMessageDto.class));
    }
    public void setUserMessageDto(UserMessageDto userMessageDto) {
        this.userMessageDto = userMessageDto;
    }
}
