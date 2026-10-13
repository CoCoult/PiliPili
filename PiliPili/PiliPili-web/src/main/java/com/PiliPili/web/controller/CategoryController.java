package com.PiliPili.web.controller;

import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.pojo.Category;
import com.PiliPili.service.CategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController extends BaseController {

    /**
     * 获取全部分类
     */
    @Resource
    CategoryService categoryService;

    @RequestMapping("/getCategory")
    public Result GetCategory() {
        List<Category> categoryList = this.categoryService.GetCategoryFromRedis();
        return Result.success(categoryList);
    }



}
