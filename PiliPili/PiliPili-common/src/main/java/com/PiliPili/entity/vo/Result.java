package com.PiliPili.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Result<T> implements Serializable {
    private int code;    // 状态码
    private String msg;  // 消息提示
    private T data;      // 响应数据
    private long timestamp; // 时间戳

    // 常用状态码定义
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int ERROR = 500;

    // 构造函数私有化
    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS, "操作成功", null);
    }

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS, "操作成功", data);
    }

    // 失败响应（默认状态码）
    public static <T> Result<T> error(String msg) {
        return new Result<>(ERROR, msg, null);
    }

    // 自定义状态码失败响应
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    // 链式调用设置消息
    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    // 链式调用设置数据
    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    // 常用响应快速生成方法
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(BAD_REQUEST, msg, null);
    }

    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(UNAUTHORIZED, msg, null);
    }

    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(FORBIDDEN, msg, null);
    }

    public static <T> Result<T> notFound(String msg) {
        return new Result<>(NOT_FOUND, msg, null);
    }

    // Getter方法（Setter根据需要添加）
    public int getCode() { return code; }
    public String getMsg() { return msg; }
    public T getData() { return data; }
    public long getTimestamp() { return timestamp; }

    // 判断是否成功（根据业务逻辑可能需要调整）
    public boolean isSuccess() {
        return code == SUCCESS;
    }

    // 可选：添加分页支持
    public static class PageResult<T> extends Result<List<T>> {
        private int pageNum;
        private int pageSize;
        private long total;

        private PageResult(int code, String msg, List<T> data,
                           int pageNum, int pageSize, long total) {
            super(code, msg, data);
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.total = total;
        }

        public static <T> PageResult<T> page(List<T> data,
                                             int pageNum,
                                             int pageSize,
                                             long total) {
            return new PageResult<>(SUCCESS, "操作成功", data,
                    pageNum, pageSize, total);
        }

        // Getter方法
        public int getPageNum() { return pageNum; }
        public int getPageSize() { return pageSize; }
        public long getTotal() { return total; }
    }
}
