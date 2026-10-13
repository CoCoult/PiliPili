package com.PiliPili.service.impl;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.pojo.Category;
import com.PiliPili.entity.query.CategoryQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.CategoryMapper;
import com.PiliPili.service.CategoryService;
import com.PiliPili.service.VideoInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    CategoryMapper categoryMapper;

    @Resource
    RedisComponent redisComponent;

    @Resource
    VideoInfoService videoInfoService;

    /**
     * 获取全部分类
     * @return
     */
    @Override
    public List<Category> GetAllCategory(CategoryQuery categoryQuery) {
        List<Category> categoryList = categoryMapper.GetAllCategory();
        if (categoryQuery.getCategoryLine2Tree()){
            categoryList = ChangeLine2Tree(categoryList, Constants.CATEGORY_LEVEL_ONE);
        }
        return categoryList;
    }

    /**
     * 将数组转为树状
     */
    public List<Category> ChangeLine2Tree(List<Category> categoryList, Integer categoryId){
        List<Category> categoryList1 = new ArrayList<>();
        for (Category category: categoryList) {
            if (category.getPCategoryId() != null && category.getCategoryId() != null &&
                    category.getPCategoryId().equals(categoryId)){
                category.setChildren(ChangeLine2Tree(categoryList, category.getCategoryId()));
                categoryList1.add(category);
            }
        }
        return categoryList1;
    }

    /**
     * 新增分类
     * @param category
     */
    @Override
    public void SaveCategoryInfo(Category category) {
        Category category1 = this.categoryMapper.GetCategoryByCode(category.getCategoryCode());
        //若是新插入分类但分类已存在 或者 更新分类但是分类编号重复
        if (category.getCategoryId() == null && category1 != null ||
                category.getCategoryId() != null && category1 != null && !category1.getCategoryId().equals(category.getCategoryId())){
            throw new BusinessException("分类编号已存在");
        }
        if (category.getCategoryId() == null){
            //获取最大序号为编号排序
            Integer sort = this.categoryMapper.GetMaxCategorySort(category.getPCategoryId());
            category.setSort(sort + 1);
            this.categoryMapper.InsertCategory(category);
        }else {
            this.categoryMapper.UpdateCategoryInfo(category);
        }
        //刷新缓存
        SaveCategory2Redis();
    }

    /**
     * 根据categoryId删除分类信息
     *
     * @param categoryId
     */
    @Override
    public void DeleteCategoryId(Integer categoryId) {
        //分类有视频则无法删除分类
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        Integer count = this.videoInfoService.GetVideoCount(videoInfoQuery);
        if (count > 0) {
            throw new BusinessException("分类下存在视频，无法删除!");
        }

        Category category = this.categoryMapper.GetCategoryById(categoryId);
        //若删除的是一级分类则一并删除子分类
        if (Objects.equals(category.getPCategoryId(), Constants.CATEGORY_LEVEL_ONE)){
            this.categoryMapper.DeleteCategoryPid(categoryId);
            this.categoryMapper.DeleteCategoryId(categoryId);
        }
        else {
            //删除二级分类
            this.categoryMapper.DeleteCategoryId(categoryId);
        }
        //刷新缓存
        SaveCategory2Redis();
    }

    /**
     * 将分类存储至redis缓存
     */
    public void SaveCategory2Redis(){
        CategoryQuery categoryQuery = new CategoryQuery();
        categoryQuery.setCategoryLine2Tree(true);
        List<Category> categoryList = GetAllCategory(categoryQuery);
        redisComponent.SaveCategory2Redis(categoryList);
    }

    /**
     * 从redis获取分类
     */
    @Override
    public List<Category> GetCategoryFromRedis() {
        if (redisComponent.GetCategoryFromRedis() == null) {
            SaveCategory2Redis();
        }
        return redisComponent.GetCategoryFromRedis();
    }
}
