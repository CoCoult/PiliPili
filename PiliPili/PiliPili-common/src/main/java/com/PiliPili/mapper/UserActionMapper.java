package com.PiliPili.mapper;

import com.PiliPili.entity.dto.CountInfoDto;
import com.PiliPili.entity.pojo.UserAction;
import com.PiliPili.entity.query.UserActionQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserActionMapper {

    /**
     * 保存用户行为
     */
    void SaveUserAction(UserAction userAction);

    /**
     * 获取用户行为
     */
    UserAction GetUserActionById(String videoId, Integer commentId, String userId, Integer actionType);

    /**
     * 删除用户行为
     */
    void DeleteUserAction(Integer actionId);

    /**
     * 获取当前视频所有用户行为
     */
    List<UserAction> GetUserActionByVideoId(UserActionQuery userActionQuery);

    /**
     * 获取收藏视频信息
     * @param userActionQuery
     * @return
     */
    List<UserAction> GetUserActionList(UserActionQuery userActionQuery);

    /**
     * 获取用户总视频点赞数和总播放数
     * @param userId
     * @return
     */
    CountInfoDto GetUserActionCount(@Param("userId") String userId);

    /**
     * 获取用户互动行为
     */
    List<UserAction> GetUserActionListByUserId(@Param("userId") String userId);
}
