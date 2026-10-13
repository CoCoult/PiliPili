package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.annotation.RecordUserMessage;
import com.PiliPili.entity.enums.MessageEnum;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.pojo.UserAction;
import com.PiliPili.service.UserActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

@RestController
@RequestMapping("/userAction")
@Validated
@Slf4j
public class UserActionController extends BaseController{

    @Resource
    UserActionService userActionService;

    /**
     * 保存用户行为(点赞收藏投币）
     * @param videoId
     * @param actionType
     * @param actionCount
     * @param commentId
     * @return
     */
    @RequestMapping("/doAction")
    @GlobalInterceptor(checkLogin = true)
    @RecordUserMessage(messageType = MessageEnum.LIKE)
    public Result DoAction(@NotEmpty String videoId,
                           @NotNull Integer actionType,
                           @Max(2) @Min(1) Integer actionCount,
                           Integer commentId) {
        UserAction userAction = new UserAction();
        userAction.setVideoId(videoId);
        userAction.setActionType(actionType);
        actionCount = actionCount == null ? 0 : actionCount;
        userAction.setActionCount(actionCount);
        if (commentId == null){
            commentId = 0;
        }
        userAction.setCommentId(commentId);
        userAction.setUserId(GetTokenUserInfo().getUserId());
        userActionService.SaveUserAction(userAction);
        return Result.success();
    }
}
