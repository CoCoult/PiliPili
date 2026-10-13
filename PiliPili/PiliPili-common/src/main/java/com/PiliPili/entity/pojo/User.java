package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

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
    private LocalDateTime birthday;

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

    //扩展
    private Integer likeCount; //用户视频总点赞数

    private Integer playCount; //用户视频总播放数
}
