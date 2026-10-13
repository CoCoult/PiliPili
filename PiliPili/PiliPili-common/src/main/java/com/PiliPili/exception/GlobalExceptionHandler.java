package com.PiliPili.exception;

import com.PiliPili.entity.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.net.BindException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 全局异常处理
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        logger.error("请求错误,请求地址{},请求信息:", request.getRequestURL(), e);

        if (e instanceof ConstraintViolationException || e instanceof BindException){
            return Result.error(600, "参数错误");
        } else if (e instanceof BusinessException) {
            return Result.error(((BusinessException) e).getCode(), e.getMessage());
        } else{
            return null;
        }
    }
}
