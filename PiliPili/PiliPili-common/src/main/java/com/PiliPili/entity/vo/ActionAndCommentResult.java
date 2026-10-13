package com.PiliPili.entity.vo;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserAction;
import lombok.Data;

import java.util.List;

@Data
public class ActionAndCommentResult {
    private PageBean commentList;

    private List<UserAction> userActionList;
}
