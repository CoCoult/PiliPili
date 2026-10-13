package com.PiliPili.service;

import com.PiliPili.entity.pojo.Category;
import com.PiliPili.entity.query.CategoryQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {

    /**
     * 获取全部分类
     */
    List<Category> GetAllCategory(CategoryQuery categoryQuery);

    /**
     * 新增分类
     */
    void SaveCategoryInfo(Category category);

    /**
     * 根据categoryId删除分类信息
     */
    void DeleteCategoryId(Integer categoryId);

    /**
     * 从redis获取分类
     */
    List<Category> GetCategoryFromRedis();
}
