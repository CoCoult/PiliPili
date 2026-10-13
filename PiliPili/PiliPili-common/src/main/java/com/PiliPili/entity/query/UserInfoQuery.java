package com.PiliPili.entity.query;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfoQuery {


    /**
     * 用户Id
     */
    private String userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String passWord;

    /**
     * 性别 0女1男
     */
    private Integer gender;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 学校
     */
    private String school;

    /**
     * 简介
     */
    private String selfIntroduction;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录的时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登陆的ip
     */
    private String lastLoginIp;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 公告
     */
    private String notice;

    /**
     * 总硬币
     */
    private Integer totalCoin;

    /**
     * 当前硬币
     */
    private Integer currentCoin;

    /**
     * 主题
     */
    private Integer theme;

    private List<String> userIdList;

    // 排序设置
    private String orderByField;     // 排序字段
    private boolean ascending = true; // 是否升序（默认true）

    // 分页参数
    private Integer pageNumber = 1;  // 当前页码（默认1）
    private Integer pageSize = 10;   // 每页数量（默认10）
}
