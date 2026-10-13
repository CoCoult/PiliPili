package com.PiliPili.web.controller;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sysSetting")
@Validated
@Slf4j
public class SysSettingController {
    @Resource
    RedisComponent redisComponent;

    /**
     * 获取系统设置
     */
    @RequestMapping("/getSysSetting")
    public Result GetSysSetting(){
        return Result.success(redisComponent.GetSysSettingInfo());
    }
}
