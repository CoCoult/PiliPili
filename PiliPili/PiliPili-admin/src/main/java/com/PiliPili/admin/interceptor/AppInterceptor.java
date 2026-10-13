package com.PiliPili.admin.interceptor;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AppInterceptor implements HandlerInterceptor {
    private static final String URL_COUNT = "/account";
    private static final String URL_FILE = "/file";

    @Resource
    RedisComponent redisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //若路径包含登录等请求则放行
        if (request.getRequestURI().contains(URL_COUNT)) {
            return true;
        }
        //获取请求token
        String token = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.TOKEN_ADMIN)) {
                token = cookie.getValue();
            }
        }
        //若为图片则从cookie获取token
         if (request.getRequestURI().contains(URL_FILE)) {
            token = GetTokenFromCookie(request);
        }
        if (token == null) {
            throw new BusinessException(901, "未登录或请求超时");
        }
        //token是否在redis中
        String account = redisComponent.GetAdminInfoByToken(token);
        if (account == null) {
            throw new BusinessException(901, "未登录或请求超时");
        }
        return true;
    }

    /**
     * 从cookie获取token
     */
    public String GetTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.TOKEN_ADMIN)) {
                token = cookie.getValue();
            }
        }
        return token;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
