package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.enums.VideoOrderTypeEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.query.UserActionQuery;
import com.PiliPili.entity.query.UserFocusQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.vo.UserResult;
import com.PiliPili.service.UserActionService;
import com.PiliPili.service.UserFocusService;
import com.PiliPili.service.UserService;
import com.PiliPili.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/userCenter")
@Validated
@Slf4j
public class UserCenterController extends BaseController{
    @Resource
    UserService userService;

    @Resource
    UserFocusService userFocusService;

    @Resource
    VideoInfoService videoInfoService;

    @Resource
    UserActionService userActionService;

    /**
     * 获取指定user的信息
     * @param userId
     * @return
     */
    @RequestMapping("/getUserInfo")
    public Result GetUserInfo(@NotEmpty String userId){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        UserResult userResult = this.userService.GetUserInfo(tokenUserInfoDto == null ? null : tokenUserInfoDto.getUserId(), userId);
        return Result.success(userResult);
    }

    /**
     * 更新用户信息
     * @param userName
     * @param avatar
     * @param birthday
     * @param school
     * @param self_introduction
     * @param gender
     * @param notice
     * @return
     */
    @RequestMapping("/updateUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result UpdateUserInfo(@NotEmpty @Size(max = 20) String userName,
                                 @NotEmpty @Size(max = 100) String avatar,
                                 @Size(max = 10) LocalDateTime birthday,
                                 @Size(max = 100) String school,
                                 @Size(max = 200) String self_introduction,
                                 @NotNull Integer gender,
                                 @Size(max = 200) String notice){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        User user = new User();
        user.setUserId(tokenUserInfoDto.getUserId());
        user.setUserName(userName);
        user.setAvatar(avatar);
        user.setGender(gender);
        user.setBirthday(birthday);
        user.setSchool(school);
        user.setSelfIntroduction(self_introduction);
        user.setNotice(notice);
        this.userService.UpdateUserInfo(user, tokenUserInfoDto);
        return Result.success();
    }

    /**
     * 修改用户主页背景
     * @param theme
     * @return
     */
    @RequestMapping("/updateTheme")
    @GlobalInterceptor(checkLogin = true)
    public Result GetUserInfo(@NotNull @Max(10) @Min(1) Integer theme){
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        User user = new User();
        user.setUserId(tokenUserInfoDto.getUserId());
        user.setTheme(theme);
        this.userService.UpdateUserBackground(user);
        return Result.success();
    }

    /**
     * 关注用户
     * @param focusUserId
     * @return
     */
    @RequestMapping("/focusUser")
    @GlobalInterceptor(checkLogin = true)
    public Result FocusUser(@NotEmpty String focusUserId) {
        this.userFocusService.FocusUser(GetTokenUserInfo().getUserId(), focusUserId);
        return Result.success();
    }

    /**
     * 取关用户
     * @param focusUserId
     * @return
     */
    @RequestMapping("/cancelFocusUser")
    @GlobalInterceptor(checkLogin = true)
    public Result CancelFocusUser(@NotEmpty String focusUserId) {
        this.userFocusService.CancelFocusUser(GetTokenUserInfo().getUserId(), focusUserId);
        return Result.success();
    }

    /**
     * 获取关注的用户
     * @return
     */
    @RequestMapping("/getFocusUser")
    @GlobalInterceptor(checkLogin = true)
    public Result GetFocusUser() {

        return Result.success();
    }

    /**
     * 获取已关注的用户信息
     * @param pageNum
     * @return
     */
    @RequestMapping("/getFocusUserList")
    @GlobalInterceptor(checkLogin = true)
    public Result getFocusUserList(Integer pageNum) {
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setUserId(GetTokenUserInfo().getUserId());
        userFocusQuery.setQueryType(Constants.ZERO);
        userFocusQuery.setOrderByField("focus_time desc");
        if (pageNum != null){
            userFocusQuery.setPageNumber(pageNum);
        }
        PageBean pageBean = this.userFocusService.GetUserFocusList(userFocusQuery);
        return Result.success(pageBean);
    }

    /**
     * 获取粉丝的用户信息
     * @param pageNum
     * @return
     */
    @RequestMapping("/getFansUserList")
    @GlobalInterceptor(checkLogin = true)
    public Result getFansUserList(Integer pageNum) {
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setUserId(GetTokenUserInfo().getUserId());
        userFocusQuery.setQueryType(Constants.ONE);
        userFocusQuery.setOrderByField("focus_time desc");
        if (pageNum != null){
            userFocusQuery.setPageNumber(pageNum);
        }
        PageBean pageBean = this.userFocusService.GetUserFansList(userFocusQuery);
        return Result.success(pageBean);
    }

    /**
     * 获取主页视频信息
     * @param pageNum
     * @return
     */
    @RequestMapping("/getVideoList")
    public Result GetVideoList(@NotEmpty String userId, Integer type, String videoName, Integer orderType, Integer pageNum) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (type != null) {
            videoInfoQuery.setPageSize(Constants.LENGTH_10);
        }
        VideoOrderTypeEnum videoOrderTypeEnum = null;
        //默认按照时间排序
        if (orderType == null) {
            videoOrderTypeEnum = VideoOrderTypeEnum.CREATE_TIME;
        }
        else if(orderType == 1){
            videoOrderTypeEnum = VideoOrderTypeEnum.VIDEO_PLAY;
        } else if(orderType == 2) {
            videoOrderTypeEnum = VideoOrderTypeEnum.COLLECT_VIDEO;
        }
        if (videoOrderTypeEnum == null) {
            videoOrderTypeEnum = VideoOrderTypeEnum.CREATE_TIME;
        }
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setVideoNameLike(videoName);
        if (pageNum != null) {
            videoInfoQuery.setPageNumber(pageNum);
        }
        videoInfoQuery.setOrderByField(videoOrderTypeEnum.getField() + " desc");
        PageBean videoList = this.videoInfoService.GetVideoList(videoInfoQuery);
        return Result.success(videoList);
    }

    /**
     * 获取指定用户的收藏的视频
     * @param userId
     * @param pageNum
     * @return
     */
    @RequestMapping("/getCollectionVideo")
    public Result GetCollectionVideo(@NotEmpty String userId, Integer pageNum) {
        UserActionQuery userActionQuery = new UserActionQuery();
        userActionQuery.setUserId(userId);
        if (pageNum != null) {
            userActionQuery.setPageNumber(pageNum);
        }
        //是否获取收藏视频的信息
        userActionQuery.setCollectVideoInfo(true);
        //按照收藏时间降序排序
        userActionQuery.setOrderByField("action_time desc");
        userActionQuery.setActionType(new Integer[]{ActionTypeEnum.COLLECT_VIDEO.getType()});
        PageBean resultList = this.userActionService.GetCollectVideoList(userActionQuery);
        return Result.success(resultList);
    }


}
