package com.PiliPili.service;

import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserAction;
import com.PiliPili.entity.query.UserActionQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserActionService {

    /**
     * 保存用户行为
     * @param userAction
     */
    void SaveUserAction(UserAction userAction);

    /**
     * 获取当前视频所有用户的行为
     */
    List<UserAction> GetUserActionByVideoId(UserActionQuery userActionQuery);

    /**
     * 获取用户收藏的视频信息
     * @param userActionQuery
     * @return
     */
    PageBean GetCollectVideoList(UserActionQuery userActionQuery);

}
