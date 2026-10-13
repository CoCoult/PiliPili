package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.UserFocus;
import com.PiliPili.entity.query.UserFocusQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFocusMapper {

    /**
     * 获取关注信息
     * @param userId
     * @param focusUserId
     * @return
     */
    UserFocus GetUserFocus(String userId, String focusUserId);

    /**
     * 获取关注信息
     * @param focusUserId
     * @return
     */
    UserFocus GetUserFocusByFocusUserId(String focusUserId);

    /**
     * 插入关注信息
     * @param userFocus
     */
    void InsertUserFocus(UserFocus userFocus);

    /**
     * 取消关注
     * @param userId
     * @param focusUserId
     */
    void DeleteUserFocus(String userId, String focusUserId);

    /**
     * 获取用户关注数
     * @param userId
     * @return
     */
    Integer GetUserFocusByUserId(String userId);

    /**
     * 获取用户粉丝数
     * @param userId
     * @return
     */
    Integer GetUserFansByUserId(String userId);

    /**
     * 获取关注列表
     *
     * @param userFocusQuery
     * @return
     */
    List<UserFocus> GetUserList(UserFocusQuery userFocusQuery);

}
