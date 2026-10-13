package com.PiliPili.admin.controller;

import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.query.UserInfoQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 获取用户
     * @param userInfoQuery
     * @return
     */
    @RequestMapping("/getUser")
    public Result GetsUser(UserInfoQuery userInfoQuery) {
        List<User> userList = new ArrayList<>();
        return Result.success(this.userService.GetUserInfoByPage(userInfoQuery));
    }

    /**
     * 修改用户状态
     * @param userId
     * @param status
     * @return
     */
    @RequestMapping("/changeStatus")
    public Result ChangeUserStatus(String userId, Integer status) {
        this.userService.ChangeUserStatus(userId, status);
        return Result.success();
    }
}
