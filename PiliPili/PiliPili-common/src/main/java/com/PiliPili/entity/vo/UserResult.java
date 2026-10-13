package com.PiliPili.entity.vo;

import lombok.Data;

import java.awt.*;
import java.time.LocalDateTime;

@Data
public class UserResult {
    // 基础信息
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
    private int gender;

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
    private int status;

    /**
     * 公告
     */
    private String notice;

    /**
     * 总硬币
     */
    private int totalCoin;

    /**
     * 当前硬币
     */
    private int currentCoin;

    /**
     * 主题
     */
    private int theme;

    /**
     * 是否关注
     */
    private boolean haveFocus;

    // 数据统计
    private UserStat stat;

    // 会员信息
    private VipInfo vip;

    // 勋章信息
    private List medals;

    // 直播间状态
    private LiveInfo liveRoom;

    //--------------------------
    // 内部类定义
    //--------------------------

    private Integer focusCount;  // 关注数
    private Integer fansCount;   // 粉丝数
    private Integer likeCount;      // 获赞数
    private Integer playCount;  // 视频播放量

    @Data
    public static class UserStat {

        private Integer articleViews;// 专栏阅读量
        private Integer favorite;    // 收藏数
    }

    @Data
    public static class VipInfo {
        private VipType type;        // 会员类型
        private String label;        // 会员标签
        private LocalDateTime dueDate;   // 到期时间
        private Boolean isYearVip;   // 是否年度大会员
    }

    @Data
    public static class OfficialVerify {
        private Integer type;        // 认证类型 -1:无 0:个人 1:机构
        private String desc;         // 认证描述
    }

    @Data
    public static class Medal {
        private String name;        // 勋章名称
        private String iconUrl;     // 勋章图标
        private Integer level;      // 当前等级
    }

    @Data
    public static class LiveInfo {
        private Boolean isLiving;   // 是否正在直播
        private String roomId;      // 直播间ID
        private String title;       // 直播标题
        private String cover;       // 直播间封面
    }

    //--------------------------
    // 枚举定义
    //--------------------------
    public enum VipType {
        NONE(0, "非会员"),
        VIP(1, "月度会员"),
        ANNUAL_VIP(2, "年度会员");

        private final int code;
        private final String desc;

        VipType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
