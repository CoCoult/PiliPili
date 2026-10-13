package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.pojo.UserFocus;
import com.PiliPili.entity.query.UserFocusQuery;
import com.PiliPili.entity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 根据email获取用户信息
     */
    User GetUserInfoByEmail(String email);

    /**
     * 根据userName获取用户信息
     */
    User GetUserInfoByUserName(String userName);

    /**
     * 根据email修改用户信息
     */
    void UpdateUserInfo(User user);

    /**
     * 根据id修改用户信息
     * @param user
     */
    void UpdateUserInfoByUserId(User user);

    /**
     * 根据userid获取用户信息
     */
    User GetUserInfoByUserId(String userId);
    /**
     * 插入用户信息
     */
    void InsertUserInfo(User user);

    /**
     * 更新用户数量信息
     * @param userId
     * @param changeCount
     * @param field
     */
    Integer UpdateUserCount(String userId, Integer changeCount, String field);

    /**
     * 获取用户列表
     * @param userFocusQuery
     * @return
     */
    List<UserFocus> GetUserInfoList(UserFocusQuery userFocusQuery);

    /**
     * 根据用户id获取用户列表
     * @param userInfoQuery
     * @return
     */
    List<User> GetUserInfoByUserIdList(@Param("userInfoQuery") UserInfoQuery userInfoQuery);

    /**
     * 获取全部用户数
     * @return
     */
    Integer GetAllUserCount();

    /**
     * 管理端获取前一天新增用户数
     * @param preDate
     * @return
     */
    Integer SelectNewUserCount(@Param("preDate") String preDate);

    List<User> GetUserInfo(@Param("query") UserInfoQuery query);

    /**
     * 获取所有用户Id
     * @return
     */
    List<String> GetAllUserId();
}
