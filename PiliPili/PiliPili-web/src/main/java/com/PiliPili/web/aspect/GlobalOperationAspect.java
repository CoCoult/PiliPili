package com.PiliPili.web.aspect;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.redis.RedisUtils;
import jdk.nashorn.internal.parser.Token;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.PushBuilder;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {

    @Resource
    RedisUtils redisUtils;

    @Before("@annotation(com.PiliPili.annotation.GlobalInterceptor)")
    public void InterceptorDo(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        GlobalInterceptor globalInterceptor = method.getAnnotation(GlobalInterceptor.class);

        if (globalInterceptor == null) {
            return;
        }
        if (globalInterceptor.checkLogin()) {
            checkLogin();
        }
    }

    private void checkLogin() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        if (token == null) {
            throw new BusinessException(901, "未登录");
        }
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(901, "未登录");
        }
    }

}
