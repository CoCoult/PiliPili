package com.PiliPili.admin.controller;

import com.PiliPili.entity.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RequestMapping("/test")
    public Result Test() {
        return Result.success();
    }
}
