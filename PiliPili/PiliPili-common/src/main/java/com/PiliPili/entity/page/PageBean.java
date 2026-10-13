package com.PiliPili.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBean {

    private long total;//总记录数
    private List rows;//数据列表
    private int pageNum;
    private int pageSize;
    private long pageTotal;

}
