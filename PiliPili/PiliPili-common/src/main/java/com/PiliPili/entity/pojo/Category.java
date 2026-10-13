package com.PiliPili.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    private Integer categoryId; // 分类ID（主键）

    private String categoryCode; // 分类编码

    private String categoryName; // 分类名称

    private Integer pCategoryId; // 父级分类ID

    private String icon; // 分类图标

    private String background; // 背景

    private Integer sort; // 排序号（tinyint映射为Integer）

    private List<Category> children; // 保存当前分类的子分类

}
