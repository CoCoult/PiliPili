package com.PiliPili.mapper;

import com.PiliPili.entity.dto.UserMessageCountDto;
import com.PiliPili.entity.pojo.UserMessage;
import com.PiliPili.entity.query.UserMessageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMessageMapper {

    /**
     * 查询信息条数
     * @param userMessageQuery
     * @return
     */
    Integer SelectCountByParam(UserMessageQuery userMessageQuery);

    /**
     * 插入用户消息
     * @param userMessage
     */
    void InsertUserMessage(UserMessage userMessage);

    /**
     * 分组查消息类型对应的消息数量
     * @param userId
     * @return
     */
    List<UserMessageCountDto> SelectCountGroup(String userId);

    /**
     * 更新消息状态为已读
     * @param userMessageQuery
     * @param userMessage
     */
    void UpdateUserMessageReadStatus(@Param("userMessageQuery") UserMessageQuery userMessageQuery, @Param("userMessage") UserMessage userMessage);

    /**
     * 获取指定消息类型的消息列表
     * @param userMessageQuery
     * @return
     */
    List<UserMessage> SelectUserMessageList(@Param("userMessageQuery") UserMessageQuery userMessageQuery);

    /**
     * 删除用户消息
     * @param userMessageQuery
     */
    void DeleteUserMessageByUserIdAndMessageId(@Param("userMessageQuery") UserMessageQuery userMessageQuery);
}


