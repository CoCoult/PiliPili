package com.PiliPili.admin.controller;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.utils.StringToolsUtils;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
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
    AppConfig appConfig;

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
     * 管理员登录
     */
    @RequestMapping("/login")
    public Result Login(HttpServletResponse httpServletResponse,
                        @NotEmpty String account,
                        @NotEmpty String password,
                        @NotEmpty String checkCodeKey,
                        @NotEmpty String checkCode) {
        try {
            // 对比图片验证码
            if (!redisComponent.GetCheckCodeKey(checkCodeKey).equalsIgnoreCase(checkCode)) {
                throw new BusinessException("图片验证码不正确");
            }
            // 对比账号密码是否正确
            if (!appConfig.getAdminAccount().equals(account) || !StringToolsUtils.EncodeByMD5(appConfig.getAdminPassword()).equals(password)) {
                throw new BusinessException("账号或密码不正确");
            }
            //将账号存入redis
            String token = redisComponent.SaveTokenAdmin(account);
            //保存cookie
            SaveTokenToCookieAdmin(httpServletResponse, token);
            return Result.success(account);
        } finally {
            redisComponent.DeleteCheckCode(checkCodeKey);
        }
    }

    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    public Result logout(HttpServletResponse response) {
        //删除cookie和redis中的管理员信息
        DeleteUserTokenAdmin(response);
        return Result.success("退出登录");
    }

}
