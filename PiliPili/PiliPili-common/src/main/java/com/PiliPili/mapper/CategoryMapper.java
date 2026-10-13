package com.PiliPili.mapper;

import com.PiliPili.entity.pojo.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 获取全部分类
     */
    List<Category> GetAllCategory();

    /**
     * 根据Id获取分类
     */
    Category GetCategoryById(Integer categoryId);

    /**
     * 根据Pid获取分类
     */
    Category GetCategoryByPid(Integer pCategoryId);

    /**
     * 新增分类
     */
    void InsertCategory(Category category);

    /**
     * 根据categoryCode获取分类信息
     */
    Category GetCategoryByCode(String categoryCode);

    /**
     * 根据categoryId修改分类信息
     */
    void UpdateCategoryInfo(Category category);

    /**
     * 获取最大的sort
     */
    Integer GetMaxCategorySort(Integer pCategoryId);

    /**
     * 根据categoryId删除分类信息
     */
    void DeleteCategoryId(Integer categoryId);

    /**
     * 删除所有pCategoryId分类信息
     */
    void DeleteCategoryPid(Integer categoryPid);
}
