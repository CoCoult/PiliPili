package com.PiliPili.admin.controller;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.dto.SysSettingDto;
import com.PiliPili.entity.vo.Result;
import org.apache.commons.io.output.AppendableOutputStream;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/setting")
public class SettingController {
    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取系统设置
     * @return
     */
    @RequestMapping("/getSetting")
    public Result GetSysSetting() {
        return Result.success(this.redisComponent.GetSysSettingInfo());
    }

    /**
     * 保存系统设置
     * @param sysSettingDto
     * @return
     */
    @RequestMapping("/saveSetting")
    public Result SaveSetting(SysSettingDto sysSettingDto) {
        this.redisComponent.SaveSysSetting(sysSettingDto);
        return Result.success();
    }
}
