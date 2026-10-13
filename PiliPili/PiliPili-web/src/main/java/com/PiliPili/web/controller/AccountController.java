package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.dto.UserCountDto;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.service.UserService;
import com.PiliPili.utils.StringToolsUtils;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息controller
 */
@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends BaseController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserService userService;

    /**
     * 注册图片验证码
     */
    @RequestMapping("/checkCode")
    public Result CheckCode() {
        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(100, 42);
        String code = arithmeticCaptcha.text();
        //验证码存入redis并返回唯一标识
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = arithmeticCaptcha.toBase64();
        //返回验证码图片与标识
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return Result.success(result);
    }

    /**
     * 用户注册
     *
     * @param email        邮箱
     * @param userName     姓名
     * @param password     密码
     * @param checkCodeKey 验证码key
     * @param checkCode    用户输入的验证码
     */
    @RequestMapping("/register")
    public Result Register(@NotEmpty @Email @Size(max = 150) String email,
                           @NotEmpty @Size(max = 20) String userName,
                           @NotEmpty String password,
                           @NotEmpty String checkCodeKey,
                           @NotEmpty String checkCode) {

        try {
            if (!redisComponent.GetCheckCodeKey(checkCodeKey).equalsIgnoreCase(checkCode)) {
                throw new BusinessException("图片验证码不正确");
            }
            userService.InsertUserInfo(email, userName, password);
        } finally {
            redisComponent.DeleteCheckCode(checkCodeKey);
        }
        return Result.success();
    }

    /**
     * 用户登录
     */
    @RequestMapping("/login")
    public Result Login(HttpServletResponse httpServletResponse,
                        @NotEmpty @Email @Size(max = 150) String email,
                        @NotEmpty String password,
                        @NotEmpty String checkCodeKey,
                        @NotEmpty String checkCode) {
        try {
            // 对比图片验证码
            if (!redisComponent.GetCheckCodeKey(checkCodeKey).equalsIgnoreCase(checkCode)) {
                throw new BusinessException("图片验证码不正确");
            }
            String ip = getIpAddr(); //获取用户登陆时的IP
            TokenUserInfoDto tokenUserInfoDto = userService.LoginByEmail(email, password, ip); //用户信息存入redis
            SaveTokenToCookie(httpServletResponse, tokenUserInfoDto.getToken()); //保存用户cookie
            return Result.success(tokenUserInfoDto);
        } finally {
            redisComponent.DeleteCheckCode(checkCodeKey);
        }
    }

    /**
     * 自动登录
     */
    @RequestMapping("/autoLogin")
    public Result AutoLogin(HttpServletResponse response) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        if (tokenUserInfoDto == null) {
            return Result.success();
        }
        if (tokenUserInfoDto.getExpireTime() - System.currentTimeMillis() < (Constants.REDIS_KEY_EXPIRE_TIME * 60 * 24)) {
            redisComponent.SaveTokenUserInfo(tokenUserInfoDto);
            SaveTokenToCookie(response, tokenUserInfoDto.getToken());
        }
        return Result.success(tokenUserInfoDto);
    }

    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    public Result logout(HttpServletResponse response) {
        //删除cookie和redis中的用户信息
        DeleteUserToken(response);
        return Result.success("退出登录");
    }

    /**
     * 获取用户粉丝、关注、硬币数
     * @return
     */
    @RequestMapping("/getUserCountInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result GetUserCountInfo() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();
        UserCountDto userCountDto = new UserCountDto();
        userCountDto = this.userService.GetUserCountInfo(tokenUserInfoDto.getUserId());

        return Result.success(userCountDto);
    }
}
