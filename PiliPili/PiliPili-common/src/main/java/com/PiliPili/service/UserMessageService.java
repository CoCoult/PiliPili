package com.PiliPili.service;

import com.PiliPili.entity.dto.UserMessageCountDto;
import com.PiliPili.entity.enums.MessageEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserMessage;
import com.PiliPili.entity.query.UserMessageQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserMessageService {

    /**
     * 保存用户操作产生的消息
     *
     * @param videoId
     * @param SendUserId
     * @param messageEnum
     * @param contents
     * @param replyCommentId
     */
    void SaveUserMessage(String videoId, String SendUserId, MessageEnum messageEnum, String contents, Integer replyCommentId);

    /**
     * 获取指定用户未读信息数
     * @param userMessageQuery
     * @return
     */
    Integer GetNotReadMessageCount(UserMessageQuery userMessageQuery);

    /**
     * 获取根据消息类型分组的消息数量
     * @param userId
     * @return
     */
    List<UserMessageCountDto> GetNotReadMessageCountGroup(String userId);

    /**
     * 更新消息为已读
     * @param userMessageQuery
     * @param userMessage
     */
    void UpdateUserMessageReadStatus(UserMessageQuery userMessageQuery, UserMessage userMessage);

    /**
     * 获取指定消息类型的消息列表
     * @param userMessageQuery
     * @return
     */
    PageBean GetUserMessageList(UserMessageQuery userMessageQuery);

    /**
     * 删除用户消息
     * @param userMessageQuery
     */
    void DeleteUserMessageByUserIdAndMessageId(UserMessageQuery userMessageQuery);
}
