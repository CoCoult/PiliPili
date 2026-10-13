package com.PiliPili.service.impl;

import com.PiliPili.entity.dto.UserMessageCountDto;
import com.PiliPili.entity.dto.UserMessageDto;
import com.PiliPili.entity.enums.MessageEnum;
import com.PiliPili.entity.enums.UserMessageReadEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.UserMessage;
import com.PiliPili.entity.pojo.VideoComment;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.query.UserMessageQuery;
import com.PiliPili.mapper.UserMessageMapper;
import com.PiliPili.mapper.VideoCommentMapper;
import com.PiliPili.mapper.VideoInfoMapper;
import com.PiliPili.mapper.VideoInfoPostMapper;
import com.PiliPili.service.UserMessageService;
import com.PiliPili.utils.JsonUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    UserMessageMapper userMessageMapper;

    @Resource
    VideoInfoMapper videoInfoMapper;

    @Resource
    VideoCommentMapper videoCommentMapper;

    @Resource
    VideoInfoPostMapper videoInfoPostMapper;

    /**
     * 保存用户操作产生的消息
     *
     * @param videoId
     * @param sendUserId //产生消息的用户的id
     * @param messageEnum
     * @param contents
     * @param replyCommentId
     */
    @Override
    @Async //异步保存消息失败不影响逻辑
    public void SaveUserMessage(String videoId, String sendUserId, MessageEnum messageEnum, String contents, Integer replyCommentId) {
        VideoInfo videoInfo = this.videoInfoMapper.GetVideoInfoByVideoId(videoId);
        //视频不存在
        if (videoInfo == null) {
            return;
        }
        //视频上传者的userId,即应当收到消息的user
        String userId = videoInfo.getUserId();
        //同一个用户收藏、点赞已经记录过则不再记录，避免重复消息
        //TODO 是否处理评论点赞
        //若消息类型为视频点赞或者收藏
        if (ArrayUtils.contains(new Integer[]{MessageEnum.LIKE.getType(), MessageEnum.COLLECTION.getType()}, messageEnum.getType())) {
            UserMessageQuery userMessageQuery = new UserMessageQuery();
            userMessageQuery.setUserId(userId);
            userMessageQuery.setVideoId(videoId);
            userMessageQuery.setMessageType(messageEnum.getType());
            Integer count = userMessageMapper.SelectCountByParam(userMessageQuery);
            if (count != 0) {
                return;
            }
        }
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(userId);
        userMessage.setVideoId(videoId);
        userMessage.setSendUserId(sendUserId);
        userMessage.setMessageType(messageEnum.getType());
        userMessage.setReadType(UserMessageReadEnum.NOT_READ.getCode());
        userMessage.setCreateTime(LocalDateTime.now());
        UserMessageDto userMessageDto = new UserMessageDto();
        userMessageDto.setMessageContents(contents);

        //二级回复评论特殊处理
        //消息为评论
        if (replyCommentId != null) {
            VideoComment videoComment = this.videoCommentMapper.GetCommentByCommentId(replyCommentId);
            if (videoComment != null) {
                //提醒发布评论人
                userId = videoComment.getUserId();
                //获取评论内容
                userMessageDto.setMessageContentReply(videoComment.getContent());
            }
            //不能给自己发消息
            if (userId.equals(sendUserId)) {
                return;
            }
        }

        //处理系统消息
        if (MessageEnum.SYSTEM == messageEnum) {
            VideoInfoPost videoInfoPost = this.videoInfoPostMapper.GetVideoPostInfoByVideoId(videoId);
            //获取审核状态
            userMessageDto.setAuditStatus(videoInfoPost.getStatus());
        }

        userMessage.setUserId(userId);
        userMessage.setExtendJson(JsonUtils.convertObjToJson(userMessageDto));
        this.userMessageMapper.InsertUserMessage(userMessage);
    }

    /**
     * 获取指定用户未读信息数
     *
     * @param userMessageQuery
     * @return
     */
    @Override
    public Integer GetNotReadMessageCount(UserMessageQuery userMessageQuery) {
        return this.userMessageMapper.SelectCountByParam(userMessageQuery);
    }

    /**
     * 获取根据消息类型分组的消息数量
     *
     * @param userId
     * @return
     */
    @Override
    public List<UserMessageCountDto> GetNotReadMessageCountGroup(String userId) {
        return this.userMessageMapper.SelectCountGroup(userId);
    }

    /**
     * 更新消息为已读
     *
     * @param userMessageQuery
     * @param userMessage
     */
    @Override
    public void UpdateUserMessageReadStatus(UserMessageQuery userMessageQuery, UserMessage userMessage) {
        this.userMessageMapper.UpdateUserMessageReadStatus(userMessageQuery, userMessage);
    }

    /**
     * 获取指定消息类型的消息列表
     *
     * @param userMessageQuery
     * @return
     */
    @Override
    public PageBean GetUserMessageList(UserMessageQuery userMessageQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(userMessageQuery.getPageNum(), userMessageQuery.getPageSize());
        List<UserMessage> userMessageList = this.userMessageMapper.SelectUserMessageList(userMessageQuery);
        Page<UserMessage> userMessagePage = (Page<UserMessage>) userMessageList;
        pageBean.setTotal(userMessagePage.getTotal());
        pageBean.setRows(userMessagePage.getResult());
        pageBean.setPageTotal(userMessagePage.getPages());
        pageBean.setPageNum(userMessageQuery.getPageNum());
        pageBean.setPageSize(userMessageQuery.getPageSize());
        return pageBean;
    }

    /**
     * 删除用户消息
     *
     * @param userMessageQuery
     */
    @Override
    public void DeleteUserMessageByUserIdAndMessageId(UserMessageQuery userMessageQuery) {
        this.userMessageMapper.DeleteUserMessageByUserIdAndMessageId(userMessageQuery);
    }
}
