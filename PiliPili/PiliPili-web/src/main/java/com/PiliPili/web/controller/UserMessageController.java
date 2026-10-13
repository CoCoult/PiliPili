package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.dto.UserMessageCountDto;
import com.PiliPili.entity.enums.UserMessageReadEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserMessage;
import com.PiliPili.entity.query.UserMessageQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.UserMessageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/message")
public class UserMessageController extends BaseController{

    @Resource
    UserMessageService userMessageService;

    /**
     * 获取当前登录用户未读的消息数
     * @return
     */
    @RequestMapping("/getMessageCount")
    @GlobalInterceptor(checkLogin = true)
    public Result GetMessageCount() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageQuery.setReadType(UserMessageReadEnum.NOT_READ.getCode());
        Integer count = this.userMessageService.GetNotReadMessageCount(userMessageQuery);
        return Result.success(count);
    }

    /**
     * 获取不同消息分组
     * @return
     */
    @RequestMapping("/getMessageCountGroup")
    @GlobalInterceptor(checkLogin = true)
    public Result GetMessageCountGroup() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        List<UserMessageCountDto> userMessageCountDtoList = this.userMessageService.GetNotReadMessageCountGroup(tokenUserInfoDto.getUserId());
        return Result.success(userMessageCountDtoList);
    }

    /**
     * 已读指定消息类型的全部消息
     * @param messageType
     * @return
     */
    @RequestMapping("/readAll")
    @GlobalInterceptor(checkLogin = true)
    public Result ReadAll(Integer messageType) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageQuery.setMessageType(messageType);

        UserMessage userMessage = new UserMessage();
        userMessage.setReadType(UserMessageReadEnum.READ.getCode());
        this.userMessageService.UpdateUserMessageReadStatus(userMessageQuery, userMessage);
        return Result.success();
    }

    /**
     * 获取指定消息类型的消息列表
     * @param messageType
     * @param pageNum
     * @return
     */
    @RequestMapping("/getUserMessageList")
    @GlobalInterceptor(checkLogin = true)
    public Result GetUserMessageList(Integer messageType, Integer pageNum) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageQuery.setMessageType(messageType);
        if (pageNum != null) {
            userMessageQuery.setPageNum(pageNum);

        }
        userMessageQuery.setOrderByField("message_id asc");
        PageBean pageBean = this.userMessageService.GetUserMessageList(userMessageQuery);
        return Result.success(pageBean);
    }

    /**
     * 删除用户的消息
     * @param messageId
     * @return
     */
    @RequestMapping("/delUserMessage")
    @GlobalInterceptor(checkLogin = true)
    public Result DelUserMessage(Integer messageId) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(tokenUserInfoDto.getUserId());
        userMessageQuery.setMessageId(messageId);
        this.userMessageService.DeleteUserMessageByUserIdAndMessageId(userMessageQuery);
        return Result.success();
    }
}
