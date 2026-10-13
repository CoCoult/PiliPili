package com.PiliPili.admin.controller;

import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.pojo.Category;
import com.PiliPili.entity.query.CategoryQuery;
import com.PiliPili.service.CategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
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
        CategoryQuery categoryQuery = new CategoryQuery();
        categoryQuery.setCategoryLine2Tree(true);
        List<Category> categoryList = this.categoryService.GetAllCategory(categoryQuery);
        return Result.success(categoryList);
    }

    /**
     * 修改，新增分类
     * @param pCategoryId*
     * @param categoryId*
     * @param categoryCode*
     * @param categoryName*
     * @param icon*
     */
    @RequestMapping("/saveCategory")
    public Result SaveCategory(@NotNull Integer pCategoryId,
                               Integer categoryId,
                               @NotNull String categoryCode,
                               @NotNull String categoryName,
                               String icon,
                               String background) {

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setPCategoryId(pCategoryId);
        category.setCategoryCode(categoryCode);
        category.setCategoryName(categoryName);
        category.setIcon(icon);
        category.setBackground(background);

        this.categoryService.SaveCategoryInfo(category);
        return Result.success();
    }

    /**
     * 删除分类
     * @param categoryId
     * @return
     */
    @RequestMapping("/deleteCategory")
    public Result DeleteCategory(@NotNull Integer categoryId){
        this.categoryService.DeleteCategoryId(categoryId);
        return Result.success();
    }



}
