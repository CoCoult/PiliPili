package com.PiliPili.service;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserFocus;
import com.PiliPili.entity.query.UserFocusQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserFocusService {

    /**
     * 关注用户
     * @param userId
     * @param focusUserId
     */
    void FocusUser(String userId, String focusUserId);

    /**
     * 取关用户
     * @param userId
     * @param focusUserId
     */
    void CancelFocusUser(String userId, String focusUserId);

    /**
     * 获取用户关注数
     *
     * @param userId
     * @return
     */
    Integer GetUserFocus(String userId);

    /**
     * 获取用户粉丝数
     *
     * @param userId
     * @return
     */
    Integer GetUserFans(String userId);

    /**
     * 获取关注用户列表
     * @param userFocusQuery
     * @return
     */
    PageBean GetUserFocusList(UserFocusQuery userFocusQuery);

    /**
     * 获取粉丝用户列表
     * @param userFocusQuery
     * @return
     */
    PageBean GetUserFansList(UserFocusQuery userFocusQuery);
}
