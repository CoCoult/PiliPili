package com.PiliPili.aspect;

import com.PiliPili.annotation.RecordUserMessage;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.enums.MessageEnum;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

@Component
@Aspect
@Slf4j
public class UserMassageOperationAspect {
    @Resource
    RedisComponent redisComponent;

    @Resource
    UserMessageService userMessageService;

    private static final String PARAMETER_VIDEO_ID = "videoId";
    //操作类型
    private static final String PARAMETER_ACTION_TYPE = "actionType";
    //评论内容
    private static final String PARAMETER_CONTENT = "content";
    //所回复人的评论id
    private static final String PARAMETER_REPLY_COMMENT_ID = "replyCommentId";
    //视频审核拒绝原因
    private static final String PARAMETER_AUDIT_REASON = "reason";


    @Around("@annotation(com.PiliPili.annotation.RecordUserMessage)")
    public Result InterceptorDo(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        Result result = (Result) proceedingJoinPoint.proceed();

        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        RecordUserMessage recordUserMessage = method.getAnnotation(RecordUserMessage.class);

        if (recordUserMessage != null) {
            SaveMessage(recordUserMessage, proceedingJoinPoint.getArgs(), method.getParameters());
        }
        return result;
    }

    /**
     * 保存消息
     */
    private void SaveMessage(RecordUserMessage recordUserMessage, Object[] args, Parameter[] parameters) {
        String videoId = null;
        Integer actionType = null;
        Integer replyCommentId = null;
        String content = null;
        String reason = null;
        //解析必要的参数
        for (int i = 0; i < parameters.length; i++) {
            if (PARAMETER_VIDEO_ID.equals(parameters[i].getName())) {
                videoId = (String) args[i];
            } else if (PARAMETER_ACTION_TYPE.equals(parameters[i].getName())) {
                actionType = (Integer) args[i];
            } else if (PARAMETER_CONTENT.equals(parameters[i].getName())) {
                content = (String) args[i];
            }else if (PARAMETER_REPLY_COMMENT_ID.equals(parameters[i].getName())) {
                replyCommentId = (Integer) args[i];
            }else if (PARAMETER_AUDIT_REASON.equals(parameters[i].getName())) {
                reason = (String) args[i];
            }
        }

        //若操作为收藏视频
        MessageEnum messageEnum = recordUserMessage.messageType();
        if (ActionTypeEnum.COLLECT_VIDEO.getType().equals(actionType)) {
            messageEnum = MessageEnum.COLLECTION;
        }
        TokenUserInfoDto tokenUserInfoDto = GetUserTokenInfo();
        userMessageService.SaveUserMessage(videoId, tokenUserInfoDto == null ? null : tokenUserInfoDto.getUserId(), messageEnum, content, replyCommentId);

    }

    /**
     * 获取用户信息
     */
    private TokenUserInfoDto GetUserTokenInfo() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.GetUserInfoByToken(token);
    }
}
