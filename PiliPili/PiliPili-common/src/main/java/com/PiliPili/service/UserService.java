package com.PiliPili.service;

import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.dto.UserCountDto;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.query.UserInfoQuery;
import com.PiliPili.entity.vo.UserResult;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    /**
     * 根据email获取用户信息
     */
    User GetUserInfoByEmail(String email);

    /**
     * 插入注册用户的信息
     */
    void InsertUserInfo(String email, String userName, String passWord);

    /**
     * 用户登录
     */
    TokenUserInfoDto LoginByEmail(String email, String passWord, String ip);

    /**
     * 获取用户信息
     *
     * @param currentUserId
     * @param userId
     * @return
     */
    UserResult GetUserInfo(String currentUserId, String userId);

    /**
     * 更新用户信息
     * @param user
     */
    void UpdateUserInfo(User user, TokenUserInfoDto tokenUserInfoDto);

    /**
     * 更新用户背景
     * @param user
     */
    void UpdateUserBackground(User user);

    /**
     * 获取用户的关注、粉丝、硬币数
     * @param userId
     * @return
     */
    UserCountDto GetUserCountInfo(String userId);

    /**
     * 管理端获取前一天新增用户数
     */
    Integer GetNewUserCount(String preDate);

    /**
     * 获取用户信息
     * @param userInfoQuery
     * @return
     */
    PageBean GetUserInfoByPage(UserInfoQuery userInfoQuery);

    /**
     * 修改用户状态
     * @param userId
     * @param status
     */
    void ChangeUserStatus(String userId, Integer status);
}
