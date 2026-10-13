package com.PiliPili.entity.page;

import java.util.List;

public class Page<T> {
    // 当前页码
    private int pageNum;
    // 每页记录数
    private int pageSize;
    // 总记录数
    private long totalCount;
    // 当前页的数据列表
    private List<T> data;

    public Page(int pageNum, int pageSize, long totalCount, List<T> data) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.data = data;
    }

    //获取总页数
    public Integer GetPages() {
        Integer pages = this.data.size() / this.pageSize;
        if(this.data.size() % this.pageSize == 0) {
            return pages;
        }else {
            return pages + 1;
        }
    }


    // 获取当前页码
    public int getPageNum() {
        return pageNum;
    }

    // 设置当前页码
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    // 获取每页记录数
    public int getPageSize() {
        return pageSize;
    }

    // 设置每页记录数
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    // 获取总记录数
    public long getTotalCount() {
        return totalCount;
    }

    // 设置总记录数
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    // 获取当前页的数据列表
    public List<T> getData() {
        return data;
    }

    // 设置当前页的数据列表
    public void setData(List<T> data) {
        this.data = data;
    }

    // 计算总页数
    public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    // 获取当前页的起始记录索引
    public int getStartIndex() {
        return (pageNum - 1) * pageSize;
    }

    // 判断是否有上一页
    public boolean hasPreviousPage() {
        return pageNum > 1;
    }

    // 判断是否有下一页
    public boolean hasNextPage() {
        return pageNum < getTotalPages();
    }
}
