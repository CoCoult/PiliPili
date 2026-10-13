package com.PiliPili.service.impl;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.pojo.UserFocus;
import com.PiliPili.entity.query.UserFocusQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.UserFocusMapper;
import com.PiliPili.mapper.UserMapper;
import com.PiliPili.service.UserFocusService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserFocusServiceImpl implements UserFocusService {

    @Resource
    UserMapper userMapper;

    @Resource
    UserFocusMapper userFocusMapper;

    /**
     * 关注用户
     *
     * @param userId
     * @param focusUserId
     */
    @Override
    public void FocusUser(String userId, String focusUserId) {
        if (userId.equals(focusUserId)) {
            throw new BusinessException("不能对自己进行此操作");
        }
        UserFocus dbUserFocus = this.userFocusMapper.GetUserFocusByFocusUserId(focusUserId);
        //用户已关注
        if (dbUserFocus != null) {
            return;
        }
        User dbUser = this.userMapper.GetUserInfoByUserId(focusUserId);
        if (dbUser == null) {
            throw new BusinessException(600, "参数错误");
        }
        UserFocus userFocus = new UserFocus();
        userFocus.setUserId(userId);
        userFocus.setFocusUserId(focusUserId);
        userFocus.setFocusTime(LocalDateTime.now());
        this.userFocusMapper.InsertUserFocus(userFocus);
    }

    /**
     * 取关用户
     * @param userId
     * @param focusUserId
     */
    @Override
    public void CancelFocusUser(String userId, String focusUserId) {
        this.userFocusMapper.DeleteUserFocus(userId, focusUserId);
    }

    /**
     * 获取用户关注数
     *
     * @param userId
     * @return
     */
    @Override
    public Integer GetUserFocus(String userId) {
        return this.userFocusMapper.GetUserFocusByUserId(userId);
    }

    /**
     * 获取用户粉丝数
     *
     * @param userId
     * @return
     */
    @Override
    public Integer GetUserFans(String userId) {
        return this.userFocusMapper.GetUserFansByUserId(userId);
    }

    /**
     *
     *
     * @param userFocusQuery
     * @return
     */
    @Override
    public PageBean GetUserFocusList(UserFocusQuery userFocusQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(userFocusQuery.getPageNumber(), userFocusQuery.getPageSize());
        List<UserFocus> userList = this.userMapper.GetUserInfoList(userFocusQuery);
        PageInfo<UserFocus> userIdListPage = new PageInfo<>(userList);
        pageBean.setRows(userIdListPage.getList());
        pageBean.setTotal(userIdListPage.getTotal());
        pageBean.setPageTotal(userIdListPage.getPages());
        pageBean.setPageNum(userFocusQuery.getPageNumber());
        pageBean.setPageSize(userFocusQuery.getPageSize());
        return pageBean;
    }

    /**
     * 获取粉丝用户列表
     * @param userFocusQuery
     * @return
     */
    @Override
    public PageBean GetUserFansList(UserFocusQuery userFocusQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(userFocusQuery.getPageNumber(), userFocusQuery.getPageSize());
        List<UserFocus> userList = this.userMapper.GetUserInfoList(userFocusQuery);
        PageInfo<UserFocus> userIdListPage = new PageInfo<>(userList);
        pageBean.setRows(userIdListPage.getList());
        pageBean.setTotal(userIdListPage.getTotal());
        pageBean.setPageTotal(userIdListPage.getPages());
        pageBean.setPageNum(userFocusQuery.getPageNumber());
        pageBean.setPageSize(userFocusQuery.getPageSize());
        return pageBean;
    }
}
