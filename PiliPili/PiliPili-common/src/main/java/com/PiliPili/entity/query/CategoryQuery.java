package com.PiliPili.entity.query;

import com.PiliPili.entity.pojo.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryQuery {
    // 精确查询条件
    private Integer categoryId;
    private String categoryCode;
    private String categoryName;
    private Integer pCategoryId;
    private String icon;
    private String background;
    private Integer sort;
    private boolean CategoryLine2Tree;


    // 模糊查询条件
    private String categoryCodeLike;
    private String categoryNameLike;

    // 范围查询
    private Integer sortMin;
    private Integer sortMax;

    // 排序设置
    private String orderByField;
    private boolean ascending = true;

    // 分页参数
    private Integer pageNumber;
    private Integer pageSize = 10;

    //--------------------------
    // 链式条件设置方法
    //--------------------------

    public CategoryQuery categoryId(Integer categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public CategoryQuery categoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
        return this;
    }

    public CategoryQuery categoryCodeLike(String keyword) {
        this.categoryCodeLike = "%" + keyword + "%";
        return this;
    }

    public CategoryQuery categoryNameLike(String keyword) {
        this.categoryNameLike = "%" + keyword + "%";
        return this;
    }

    public CategoryQuery sortBetween(Integer min, Integer max) {
        this.sortMin = min;
        this.sortMax = max;
        return this;
    }

    public CategoryQuery orderBy(String field, boolean ascending) {
        this.orderByField = field;
        this.ascending = ascending;
        return this;
    }

    public CategoryQuery page(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        return this;
    }

    public boolean getCategoryLine2Tree(){
        return this.CategoryLine2Tree;
    }
}
