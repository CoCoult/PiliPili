package com.PiliPili.service.impl;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.enums.ActionTypeEnum;
import com.PiliPili.entity.enums.SearchTypeOrderEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.pojo.UserAction;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.UserActionQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.UserActionMapper;
import com.PiliPili.mapper.UserMapper;
import com.PiliPili.mapper.VideoCommentMapper;
import com.PiliPili.mapper.VideoInfoMapper;
import com.PiliPili.service.UserActionService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class UserActionServiceImpl implements UserActionService {

    @Resource
    UserActionMapper userActionMapper;

    @Resource
    VideoInfoMapper videoInfoMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    VideoCommentMapper videoCommentMapper;

    @Resource
    EsSearchComponent esSearchComponent;

    /**
     * 保存用户行为
     *
     * @param userAction
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void SaveUserAction(UserAction userAction) {
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(userAction.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(600, "参数错误");
        }
        ActionTypeEnum actionTypeEnum = ActionTypeEnum.GetActionType(userAction.getActionType());
        if (actionTypeEnum == null) {
            throw new BusinessException(600, "参数错误");
        }
        userAction.setVideoUserId(videoInfo.getUserId());
        userAction.setActionTime(LocalDateTime.now());
        UserAction dbuserAction = this.userActionMapper.GetUserActionById(userAction.getVideoId(), userAction.getCommentId(), userAction.getUserId(), userAction.getActionType());

        //根据操作类型进行不同操作
        switch (actionTypeEnum) {
            case LIKE_VIDEO:
            case COLLECT_VIDEO:
                //若不为空则说明当前操作为取消行为
                Integer changeCount;
                if (dbuserAction != null) {
                    this.userActionMapper.DeleteUserAction(dbuserAction.getActionId());
                    changeCount = -1;
                } else {
                    this.userActionMapper.SaveUserAction(userAction);
                    changeCount = 1;
                }
                this.videoInfoMapper.UpdateVideoCount(userAction.getVideoId(), changeCount, actionTypeEnum.getField());
                if (actionTypeEnum == ActionTypeEnum.COLLECT_VIDEO) {
                    //TODO 未测试 更新es 收藏数量
                    esSearchComponent.UpdateDocCount(userAction.getVideoId(), SearchTypeOrderEnum.VIDEO_COLLECT.getField(), changeCount);

                }
                break;
            case COIN_VIDEO: //投币视频
                if (Objects.equals(userAction.getUserId(), videoInfo.getUserId())) {
                    throw new BusinessException("不能给自己的稿件投币");
                }
                if (dbuserAction != null) {
                    throw new BusinessException("不能重复投币");
                }
                //用户账户硬币减少
                Integer updateCount = this.userMapper.UpdateUserCount(userAction.getUserId(), -userAction.getActionCount(), "current_coin");
                if (updateCount == 0) {
                    throw new BusinessException("硬币不足");
                }
                //up主硬币增加
                this.userMapper.UpdateUserCount(userAction.getVideoUserId(), userAction.getActionCount(), "current_coin");
                this.userActionMapper.SaveUserAction(userAction);
                //更新视频信息
                this.videoInfoMapper.UpdateVideoCount(userAction.getVideoId(), userAction.getActionCount(), actionTypeEnum.getField());
                break;
            case LIKE_COMMENT:
            case HATE_COMMENT:
                ActionTypeEnum inverseActionType = actionTypeEnum == ActionTypeEnum.LIKE_COMMENT ? ActionTypeEnum.HATE_COMMENT : ActionTypeEnum.LIKE_COMMENT;
                //查询相反操作是否在数据库
                UserAction inverseAction = this.userActionMapper.GetUserActionById(userAction.getVideoId(), userAction.getCommentId(), userAction.getUserId(), inverseActionType.getType());
                //若不为空则直接删除相反操作
                if (inverseAction != null) {
                    this.userActionMapper.DeleteUserAction(inverseAction.getActionId());
                }
                //若为空则检测当前操作是否在数据库
                //若在数据库则删除当前操作
                if (dbuserAction != null) {
                    this.userActionMapper.DeleteUserAction(dbuserAction.getActionId());
                } else {
                    this.userActionMapper.SaveUserAction(userAction);
                }
                //更新评论点赞/踩数
                changeCount = dbuserAction == null ? Constants.ONE : -Constants.ONE;
                Integer opposeChangeCount = -changeCount;
                this.videoCommentMapper.UpdateCommentCount(userAction.getCommentId(), actionTypeEnum.getField(), changeCount, inverseAction == null ? null : inverseActionType.getField(), opposeChangeCount);
                break;
        }
    }

    /**
     * 获取当前视频所有用户的行为
     *
     * @param userActionQuery
     */
    @Override
    public List<UserAction> GetUserActionByVideoId(UserActionQuery userActionQuery) {
        return this.userActionMapper.GetUserActionByVideoId(userActionQuery);
    }

    /**
     * 获取用户收藏的视频信息
     *
     * @param userActionQuery
     * @return
     */
    @Override
    public PageBean GetCollectVideoList(UserActionQuery userActionQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(userActionQuery.getPageNumber(), userActionQuery.getPageSize());
        List<UserAction> userActionList = this.userActionMapper.GetUserActionList(userActionQuery);
        Page<UserAction> userActionPage = (Page<UserAction>) userActionList;
        pageBean.setRows(userActionPage.getResult());
        pageBean.setTotal(userActionPage.getTotal());
        return pageBean;
    }
}
