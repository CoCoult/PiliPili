package com.PiliPili.exception;

// 基础业务异常
public class BusinessException extends RuntimeException {

    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int ERROR = 500;

    public static final int PARAMETER_ERROR = 600;

    private final int code;

    public BusinessException(){
        this.code = NOT_FOUND;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ERROR;
    }
    public int getCode() { return code; }
}

