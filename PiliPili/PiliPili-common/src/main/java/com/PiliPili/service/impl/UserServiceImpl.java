package com.PiliPili.service.impl;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.CountInfoDto;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.dto.UserCountDto;
import com.PiliPili.entity.enums.GenderEnum;
import com.PiliPili.entity.enums.StatusEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.pojo.UserFocus;
import com.PiliPili.entity.query.UserInfoQuery;
import com.PiliPili.entity.vo.UserResult;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.UserActionMapper;
import com.PiliPili.mapper.UserFocusMapper;
import com.PiliPili.mapper.UserMapper;
import com.PiliPili.service.UserService;
import com.PiliPili.utils.CopyToolsUtils;
import com.PiliPili.utils.StringToolsUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper userMapper;

    @Resource
    RedisComponent redisComponent;

    @Resource
    UserFocusMapper userFocusMapper;

    @Resource
    UserActionMapper userActionMapper;

    /**
     * 根据email获取用户信息
     *
     * @param email
     */
    @Override
    public User GetUserInfoByEmail(String email) {
        return this.userMapper.GetUserInfoByEmail(email);
    }

    /**
     * 插入注册用户的信息
     *
     * @param email
     * @param userName
     * @param passWord
     */
    @Override
    public void InsertUserInfo(String email, String userName, String passWord) {
        User user = this.userMapper.GetUserInfoByEmail(email);
        if (user != null) {
            throw new BusinessException("邮箱已注册");
        }
        User user1 = this.userMapper.GetUserInfoByUserName(userName);
        if (user1 != null) {
            throw new BusinessException("当前名称已存在");
        }
        user = new User();
        user.setUserId(StringToolsUtils.GetRandomNumber(Constants.LENGTH_10));
        user.setUserName(userName);
        user.setEmail(email);
        user.setGender(GenderEnum.UNKNOWN.ordinal());
        //前端加密密
        user.setPassWord(passWord);
        user.setCreateTime(LocalDateTime.now());
        user.setLastLoginTime(LocalDateTime.now());
        user.setStatus(StatusEnum.AUTHORIZED.ordinal());
        user.setCurrentCoin(Constants.ZERO);
        user.setTotalCoin(Constants.ZERO);
        this.userMapper.InsertUserInfo(user);
    }

    /**
     * 用户登录
     *
     * @param email    邮箱
     * @param passWord 密码
     * @param ip       地址
     */
    @Override
    public TokenUserInfoDto LoginByEmail(String email, String passWord, String ip) {
        User user = this.userMapper.GetUserInfoByEmail(email);
        if (user == null || !user.getPassWord().equalsIgnoreCase(passWord)) {
            System.out.println(passWord);
            System.out.println(user.getPassWord());
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == StatusEnum.FORBIDDEN.ordinal()) {
            throw new BusinessException("账号已被封禁");
        }
        User userInfo = new User();
        userInfo.setLastLoginTime(LocalDateTime.now());
        userInfo.setLastLoginIp(ip);
        this.userMapper.UpdateUserInfo(user);
        //复制当前用户信息并存入redis
        TokenUserInfoDto tokenUserInfoDto = CopyToolsUtils.copy(user, TokenUserInfoDto.class);
        redisComponent.SaveTokenUserInfo(tokenUserInfoDto);

        return tokenUserInfoDto;
    }

    /**
     * 获取用户信息
     *
     * @param currentUserId
     * @param userId
     * @return
     */
    @Override
    public UserResult GetUserInfo(String currentUserId, String userId) {
        User user = this.userMapper.GetUserInfoByUserId(userId);
        if (user == null) {
            throw new BusinessException(404, "error");
        }
        //返回用户粉获赞数，播放数
        CountInfoDto countInfoDto =this.userActionMapper.GetUserActionCount(userId);
        user.setPlayCount(countInfoDto.getPlayCount());
        user.setLikeCount(countInfoDto.getLikeCount());
        //获取用户关注粉丝数
        Integer focusCount = this.userFocusMapper.GetUserFocusByUserId(userId);
        Integer fansCount = this.userFocusMapper.GetUserFansByUserId(userId);
        UserResult userResult = CopyToolsUtils.copy(user, UserResult.class);
        if (currentUserId == null) {
            userResult.setHaveFocus(false);
        }
        else {
            //当前用户是否关注指定用户
            UserFocus userFocus = this.userFocusMapper.GetUserFocus(currentUserId, userId);
            if (userFocus == null) {
                //未关注
                userResult.setHaveFocus(false);
            }
            else {
                //已关注
                userResult.setHaveFocus(true);
            }
        }
        userResult.setFocusCount(focusCount);
        userResult.setFansCount(fansCount);
        return userResult;
    }

    /**
     * 更新用户信息
     *
     * @param user
     */
    @Override
    public void UpdateUserInfo(User user, TokenUserInfoDto tokenUserInfoDto) {
        User dbUser = this.userMapper.GetUserInfoByUserId(user.getUserId());
        //修改姓名需要五个硬币
        if (!dbUser.getUserName().equals(user.getUserName()) && dbUser.getCurrentCoin() < Constants.UPDATE_USER_NAME_COIN) {
            throw new BusinessException("硬币不足，无法修改昵称");
        }
        //避免修改硬币的并发问题，
        if (!dbUser.getUserName().equals(user.getUserName())) {
            Integer count = this.userMapper.UpdateUserCount(user.getUserId(), -Constants.UPDATE_USER_NAME_COIN, "current_coin");
            if (count == 0) {
                throw new BusinessException("硬币不足，无法修改昵称");
            }
        }
        this.userMapper.UpdateUserInfoByUserId(user);
        //是否发生修改，更新redis
        boolean updateTokenInfo = false;
        if (!user.getAvatar().equals(tokenUserInfoDto.getAvatar())) {
            tokenUserInfoDto.setAvatar(user.getAvatar());
            updateTokenInfo = true;
        }
        if (!user.getUserName().equals(tokenUserInfoDto.getUserName())) {
            tokenUserInfoDto.setUserName(user.getUserName());
            updateTokenInfo = true;
        }
        if (updateTokenInfo) {
            redisComponent.UpdateTokenUserInfo(tokenUserInfoDto);
        }
    }

    /**
     * 更新用户背景
     *
     * @param user
     */
    @Override
    public void UpdateUserBackground(User user) {
        this.userMapper.UpdateUserInfoByUserId(user);
    }

    /**
     * 获取用户的关注、粉丝、硬币数
     *
     * @param userId
     * @return
     */
    @Override
    public UserCountDto GetUserCountInfo(String userId) {
        UserCountDto userCountDto = new UserCountDto();
        Integer fansCount = this.userFocusMapper.GetUserFansByUserId(userId);
        Integer focusCount = this.userFocusMapper.GetUserFocusByUserId(userId);
        User userinfo = this.userMapper.GetUserInfoByUserId(userId);
        Integer currentCount = userinfo.getCurrentCoin();
        userCountDto.setCurrentCoinCount(currentCount);
        userCountDto.setFansCount(fansCount);
        userCountDto.setFocusCount(focusCount);
        return userCountDto;
    }

    /**
     * 管理端获取前一天新增用户数
     */
    @Override
    public Integer GetNewUserCount(String preDate) {
        return this.userMapper.SelectNewUserCount(preDate);
    }

    /**
     * 获取用户信息
     *
     * @param userInfoQuery
     * @return
     */
    @Override
    public PageBean GetUserInfoByPage(UserInfoQuery userInfoQuery) {
        if(userInfoQuery.getPageNumber() == null) {
            userInfoQuery.setPageNumber(1);
        }
        if(userInfoQuery.getPageSize() == null) {
            userInfoQuery.setPageSize(20);
        }
        PageBean pageBean = new PageBean();
        PageHelper.startPage(userInfoQuery.getPageNumber(), userInfoQuery.getPageSize());
        Page<User> userPage = (Page<User>) this.userMapper.GetUserInfo(userInfoQuery);
        pageBean.setPageTotal(userPage.getPages());
        pageBean.setRows(userPage.getResult());
        pageBean.setTotal(userPage.getTotal());
        pageBean.setPageNum(userPage.getPageNum());
        pageBean.setPageSize(userInfoQuery.getPageSize());
        return pageBean;
    }

    /**
     * 修改用户状态
     *
     * @param userId
     * @param status
     */
    @Override
    public void ChangeUserStatus(String userId, Integer status) {
        User user = new User();
        user.setStatus(status);
        user.setUserId(userId);
        this.userMapper.UpdateUserInfoByUserId(user);
    }
}
