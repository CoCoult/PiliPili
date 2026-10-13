package com.PiliPili.service.impl;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.SysSettingDto;
import com.PiliPili.entity.dto.UploadingFileDto;
import com.PiliPili.entity.dto.VideoInfoPostDTO;
import com.PiliPili.entity.enums.VideoFileStatusEnum;
import com.PiliPili.entity.enums.VideoFileTransferEnum;
import com.PiliPili.entity.enums.VideoRecommendEnum;
import com.PiliPili.entity.enums.VideoStatusEnum;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoInfoPost;
import com.PiliPili.entity.query.VideoFilePostQuery;
import com.PiliPili.entity.query.VideoInfoFileQuery;
import com.PiliPili.entity.query.VideoInfoPostQuery;
import com.PiliPili.entity.query.VideoInfoQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.*;
import com.PiliPili.service.VideoInfoFileService;
import com.PiliPili.service.VideoInfoPostService;
import com.PiliPili.service.VideoInfoService;
import com.PiliPili.utils.CopyToolsUtils;
import com.PiliPili.utils.FFmpegUtils;
import com.PiliPili.utils.StringToolsUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jdk.nashorn.internal.ir.BlockLexicalContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.util.FileUtil;
import org.springframework.instrument.classloading.jboss.JBossLoadTimeWeaver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoInfoPostServiceImpl implements VideoInfoPostService {

    @Resource
    EsSearchComponent esSearchComponent;

    @Resource
    RedisComponent redisComponent;

    @Resource
    VideoInfoPostMapper videoInfoPostMapper;

    @Resource
    VideoInfoFilePostMapper videoInfoFilePostMapper;

    @Resource
    VideoInfoMapper videoInfoMapper;

    @Resource
    VideoInfoFileMapper videoInfoFileMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private FFmpegUtils fFmpegUtils;

    @Resource
    private UserMapper userMapper;

    @Resource
    private VideoInfoService videoInfoService;

    //TODO 发布视频04，20min(后续配合前端再进行接口测试)

    /**
     * 保存视频
     *
     * @param videoInfoPost
     * @param videoInfoPostList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void SaveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> videoInfoPostList) {
        //若上传视频分p超过限制
        if (videoInfoPostList.size() > redisComponent.GetSysSettingInfo().getVideoPCount()) {
            throw new BusinessException(600, "参数错误");
        }
        //若videoId不为空则说明当前是修改视频操作
        if (videoInfoPost.getVideoId() != null) {
            VideoInfoPost videoInfoPost1 = this.videoInfoPostMapper.GetVideoPostInfoByVideoId(videoInfoPost.getVideoId());
            if (videoInfoPost1 == null) {
                throw new BusinessException(600, "参数错误");
            }
            //若要求改的视频还在审核或者转码中则不能修改
            if (ArrayUtils.contains(new Integer[]{
                    VideoStatusEnum.TRANSCODING.getCode(), VideoStatusEnum.PENDING_REVIEW.getCode()}, videoInfoPost1.getStatus())) {
                throw new BusinessException(600, "参数错误");
            }
        }
        String videoId = videoInfoPost.getVideoId();
        List<VideoInfoFilePost> deleteVideoList = new ArrayList<>();
        List<VideoInfoFilePost> insertVideoList = videoInfoPostList;
        //请求插入新视频
        if (videoId == null) {
            videoId = StringToolsUtils.GetRandomString(Constants.LENGTH_10);
            videoInfoPost.setCreateTime(LocalDateTime.now());
            videoInfoPost.setVideoId(videoId);
            videoInfoPost.setLastUpdateTime(LocalDateTime.now());
            videoInfoPost.setStatus(VideoStatusEnum.PENDING_REVIEW.getCode());
            this.videoInfoPostMapper.InsertVideoInfoPost(videoInfoPost);
        }
        //请求是修改视频
        else {
            VideoFilePostQuery videoFilePostQuery = new VideoFilePostQuery();
            videoFilePostQuery.setVideoId(videoId);
            videoFilePostQuery.setUserId(videoInfoPost.getUserId());
            List<VideoInfoFilePost> dbinfoPostList = this.videoInfoFilePostMapper.GetVideoInfoFilePostList(videoFilePostQuery);
            Map<String, VideoInfoFilePost> videoInfoPostMap = videoInfoPostList.stream().collect(Collectors.toMap(item -> item.getUploadId(),
                    Function.identity(), (data1, data2) -> data2));

            //对比数据库的视频文件和前端传过来的的视频文件，检测是否发生修改（增加或删除）
            boolean videoFileName = false;
            //遍历数据库的视频文件,检测前端是否有文件发生删除
            for (VideoInfoFilePost filePost : dbinfoPostList) {
                VideoInfoFilePost videoInfoFilePost = videoInfoPostMap.get(filePost.getUploadId());
                //若数据库中的视频不在前端传送的文件内，则说明该文件被删除
                if (videoInfoFilePost == null) {
                    //添加到删除表
                    deleteVideoList.add(filePost);
                } else if (!filePost.getFileName().equals(videoInfoFilePost.getFileName())) {
                    //文件名发生修改
                    videoFileName = true;
                }
            }
            //检测前端是否增加视频文件
            insertVideoList = videoInfoPostList.stream().filter(item -> item.getFileId() == null).collect(Collectors.toList());
            videoInfoPost.setLastUpdateTime(LocalDateTime.now());
            //视频信息是否发生改变
            boolean changeVideo = this.IsChangeVideoInfo(videoInfoPost);
            if (!insertVideoList.isEmpty()) {
                videoInfoPost.setStatus(VideoStatusEnum.TRANSCODING.getCode());
            } else if (changeVideo || videoFileName) {
                videoInfoPost.setStatus(VideoStatusEnum.PENDING_REVIEW.getCode());
            }
            this.videoInfoPostMapper.UpdateVideoInfoPost(videoInfoPost);
        }
        if (!deleteVideoList.isEmpty()) {
            //将要删除的fileId保存起来
            List<String> deleteFileIdList = deleteVideoList.stream().map(item -> item.getFileId()).collect(Collectors.toList());
            this.videoInfoFilePostMapper.DeleteFileByIdList(deleteFileIdList, videoInfoPost.getUserId());
            //将要删除文件的路径存储到消息队列中（redis自带轻量级消息队列功能）
            List<String> delFilePath = deleteVideoList.stream().map(item -> item.getFilePath()).collect(Collectors.toList());
            redisComponent.SaveDelFilePath2Queue(videoId, delFilePath);
        }

        Integer index = 1;
        //将新添加的分P插入视频对应的文件表
        for (VideoInfoFilePost videoFilePost : videoInfoPostList) {
            boolean isNull = false;
            //重排分P
            videoFilePost.setFileIndex(index++);
            videoFilePost.setUserId(videoInfoPost.getUserId());
            videoFilePost.setVideoId(videoId);
            //若fileId未空则为新添加的视频分P
            if (videoFilePost.getFileId() == null) {
                isNull = true;
                videoFilePost.setFileId(StringToolsUtils.GetRandomString(Constants.LENGTH_10 * 2));
                videoFilePost.setUpdateType(VideoFileStatusEnum.UPDATE.getCode());
                videoFilePost.setTransferResult(VideoFileTransferEnum.TRANSFERRING.getCode());
            }
            if (isNull) {
                this.videoInfoFilePostMapper.InsertVideoFilePost(videoFilePost);
            } else {
                this.videoInfoFilePostMapper.UpdateVideoFilePost(videoFilePost);
            }
        }

        //将新增的视频分P存到消息队列以便保存文件
        if (insertVideoList != null && !insertVideoList.isEmpty()) {
            for (VideoInfoFilePost videoInfoFilePost : insertVideoList) {
                videoInfoFilePost.setUserId(videoInfoPost.getUserId());
                videoInfoFilePost.setVideoId(videoId);
            }
            redisComponent.SaveInsertVideoFilePost2Queue(insertVideoList);
        }
    }


    /**
     * 视频信息是否发生修改
     *
     * @param videoInfoPost
     * @return
     */
    private boolean IsChangeVideoInfo(VideoInfoPost videoInfoPost) {
        VideoInfoPost dbvideoInfoPost = this.videoInfoPostMapper.GetVideoPostInfoByVideoId(videoInfoPost.getVideoId());
        //标题，封面，标签，简介
        if (!videoInfoPost.getVideoName().equals(dbvideoInfoPost.getVideoName())
                || !videoInfoPost.getVideoCover().equals(dbvideoInfoPost.getVideoCover())
                || !videoInfoPost.getTags().equals(dbvideoInfoPost.getTags())
                || !videoInfoPost.getIntroduction().equals(dbvideoInfoPost.getIntroduction())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void TransferVideoFile(VideoInfoFilePost videoInfoFilePost) {
        VideoInfoFilePost updateFilePost = new VideoInfoFilePost();
        try {
            UploadingFileDto uploadingFileDto = redisComponent.GetVideoFileInfo(videoInfoFilePost.getUserId(), videoInfoFilePost.getUploadId());
            String tempFilePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_TEMP + uploadingFileDto.getFilePath();
            File tempFile = new File(tempFilePath);
            String targetFilePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_VIDEO + uploadingFileDto.getFilePath();
            File targetFile = new File(targetFilePath);

            FileUtils.copyDirectory(tempFile, targetFile);

            //删除临时目录
            FileUtils.forceDelete(tempFile);
            redisComponent.DeleteVideoInfo(videoInfoFilePost.getUserId(), videoInfoFilePost.getUploadId());

            //合并视频文件
            String completeVideo = targetFilePath + Constants.TEMP_VIDEO_NAME;
            this.union(targetFilePath, completeVideo, true);

            //获取播放时长
            Integer duration = fFmpegUtils.GetVideoInfoDuration(completeVideo);
            updateFilePost.setDuration(duration);
            updateFilePost.setFileSize(new File(completeVideo).length());
            updateFilePost.setFilePath(Constants.FILE_VIDEO + uploadingFileDto.getFilePath());
            updateFilePost.setTransferResult(VideoFileTransferEnum.TRANSFER_SUCCESS.getCode());

            this.ConvertVideo2Ts(completeVideo);

        } catch (Exception e) {
            log.error("文件转码失败");
            updateFilePost.setTransferResult(VideoFileTransferEnum.TRANSFER_FAIL.getCode());
        } finally {
            videoInfoFilePostMapper.UpdateVideoFilePostByUploadIdAndUserId(updateFilePost, videoInfoFilePost.getUploadId(), videoInfoFilePost.getUserId());
            VideoInfoFilePost videoInfoFilePostQuery = new VideoInfoFilePost();
            videoInfoFilePostQuery.setTransferResult(VideoFileTransferEnum.TRANSFER_FAIL.getCode());
            videoInfoFilePostQuery.setVideoId(videoInfoFilePost.getVideoId());
            Integer failCount = videoInfoFilePostMapper.SelectCount(videoInfoFilePostQuery);
            if (failCount > 0) {
                VideoInfoPost updatevideoInfoPost = new VideoInfoPost();
                updatevideoInfoPost.setStatus(VideoStatusEnum.TRANSCODE_FAILED.getCode());
                updatevideoInfoPost.setLastUpdateTime(LocalDateTime.now());
                videoInfoPostMapper.UpdateByVideoId(updatevideoInfoPost, videoInfoFilePost.getVideoId());
                return;
            }
            videoInfoFilePostQuery.setTransferResult(VideoFileTransferEnum.TRANSFERRING.getCode());
            Integer transferCount = videoInfoFilePostMapper.SelectCount(videoInfoFilePostQuery);
            if (transferCount == 0) {
                //求视频总时长
                Integer duration = videoInfoFilePostMapper.SumDuration(videoInfoFilePost.getVideoId());
                VideoInfoPost videoInfoPost = new VideoInfoPost();
                videoInfoPost.setStatus(VideoStatusEnum.PENDING_REVIEW.getCode());
                videoInfoPost.setDuration(duration);
                videoInfoPostMapper.UpdateByVideoId(videoInfoPost, videoInfoFilePost.getVideoId());
            }
        }

    }

    private void ConvertVideo2Ts(String completeVideo) {
        File videoFile = new File(completeVideo);
        File tsFolder = videoFile.getParentFile();
        String codec = fFmpegUtils.GetVideoCodec(completeVideo);
        if (Constants.VIDEO_CODE_HEVC.equals(codec)) {
            String newFileName = completeVideo + Constants.VIDEO_CODE_TEMP_FILE;
            new File(completeVideo).renameTo(new File(newFileName));
            fFmpegUtils.convertHevc2mp4(newFileName, completeVideo);
            new File(newFileName).delete();
        }
        fFmpegUtils.convert2Ts(tsFolder, completeVideo);
        videoFile.delete();
    }


    /**
     * 合并视频文件
     *
     * @param dirPath
     * @param toFilePath
     * @param IsDelSource //是否删除源文件
     */
    private void union(String dirPath, String toFilePath, boolean IsDelSource) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException(600, "目录不存在");
        }
        File fileList[] = dir.listFiles();
        File targetFile = new File(toFilePath);
        try (RandomAccessFile targetFileAccess = new RandomAccessFile(targetFile, "rw")) {
            byte[] buffer = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                //创建读块文件的对象
                File chunkFile = new File(dirPath + File.separator + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(buffer)) != -1) {
                        targetFileAccess.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并文件失败", e);
                    throw new BusinessException(600, "合并文件失败");
                } finally {
                    if (readFile != null) {
                        readFile.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(600, "合并文件失败" + dirPath + "出错了");
        } finally {
            if (IsDelSource) {
                for (int i = 0; i < fileList.length; i++) {
                    fileList[i].delete();
                }
            }
        }
    }

    /**
     * 分页查询视频数据
     *
     * @param videoInfoPostQuery
     * @return
     */
    @Override
    public PageBean SelectVideoPage(VideoInfoPostQuery videoInfoPostQuery) {
        PageBean pageBean = new PageBean();
        PageHelper.startPage(videoInfoPostQuery.getPageNumber(), videoInfoPostQuery.getPageSize());
        Page<VideoInfoPostDTO> videoInfoPostPage = (Page<VideoInfoPostDTO>) videoInfoPostMapper.SelectVideoByStatus(videoInfoPostQuery);
        pageBean.setRows(videoInfoPostPage.getResult());
        pageBean.setTotal(videoInfoPostPage.getTotal());
        pageBean.setPageTotal(videoInfoPostPage.getPages());
        pageBean.setPageNum(videoInfoPostQuery.getPageNumber());
        pageBean.setPageSize(videoInfoPostQuery.getPageSize());
        return pageBean;
    }

    /**
     * 获取用某状态的视频
     *
     * @param videoInfoPostQuery
     * @return
     */
    @Override
    public Integer GetCountByUserId(VideoInfoPostQuery videoInfoPostQuery) {
        return videoInfoPostMapper.SelectCountByUserIdAndStatus(videoInfoPostQuery);

    }


    /**
     * 审核视频
     *
     * @param videoId
     * @param status
     * @param reason
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void AuditVideo(String videoId, Integer status, String reason) {
        VideoStatusEnum videoStatusEnum = VideoStatusEnum.fromCode(status);
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setStatus(status);
        videoInfoPost.setLastUpdateTime(LocalDateTime.now());

        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setStatus(VideoStatusEnum.PENDING_REVIEW.getCode());
        videoInfoPostQuery.setVideoId(videoId);
        Integer auditCount = videoInfoPostMapper.updateByParams(videoInfoPost, videoInfoPostQuery);
        if (auditCount == 0) {
            throw new BusinessException("审核失败，请稍后再试");
        }

        VideoInfoFilePost videoInfoFilePost = new VideoInfoFilePost();
        videoInfoFilePost.setUpdateType(VideoFileStatusEnum.NOT_UPDATE.getCode());

        VideoFilePostQuery videoFilePostQuery = new VideoFilePostQuery();
        videoFilePostQuery.setVideoId(videoId);
        this.videoInfoFilePostMapper.updateByParams(videoInfoFilePost, videoFilePostQuery);

        if (videoStatusEnum == VideoStatusEnum.REJECTED) {
            return;
        }

        VideoInfoPost infoPost = this.videoInfoPostMapper.selectByVideoId(videoId);

        VideoInfo dbvideoInfo = videoInfoMapper.SelectByVideoId(videoId);
        if (dbvideoInfo == null) {
            SysSettingDto sysSettingDto = redisComponent.GetSysSettingInfo();
            this.userMapper.UpdateUserCount(infoPost.getUserId(), sysSettingDto.getPostVideoCoinCount(), "current_coin");
        }
        //更新信息到正式表
        VideoInfo videoInfo = CopyToolsUtils.copy(infoPost, VideoInfo.class);
        videoInfo.setRecommendType(VideoRecommendEnum.NOT_RECOMMEND.getCode());
        videoInfo.setBarrageCount(0);
        videoInfo.setCoinCount(0);
        videoInfo.setPlayCount(0);
        videoInfo.setCollectCount(0);
        videoInfo.setLikeCount(0);
        videoInfo.setCommentCount(0);
        this.videoInfoMapper.insertOrUpdate(videoInfo);
        // 审核通过 → 视频信息写入正式表，删除详情缓存（方法内部会在事务提交后才真正删除）
        videoInfoService.deleteVideoInfoCache(videoId);
        //更新视频信息到正式文件表，先删除再添加
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        this.videoInfoFileMapper.deleteByVideoId(videoInfoFileQuery);

        VideoFilePostQuery filePostQuery = new VideoFilePostQuery();
        filePostQuery.setVideoId(videoId);
        List<VideoInfoFilePost> videoInfoFilePostList = this.videoInfoFilePostMapper.GetVideoInfoFilePostList(filePostQuery);

        List<VideoInfoFile> videoInfoFileList = CopyToolsUtils.copyList(videoInfoFilePostList, VideoInfoFile.class);
        this.videoInfoFileMapper.InsertBatch(videoInfoFileList);

        /**
         * 删除文件
         */
        List<String> filePathList = redisComponent.getDelFileList(videoId);
        if (filePathList != null) {
            for (String path : filePathList) {
                File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + path);
                if (file.exists()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        log.error("删除文件失败", e);
                    }
                }
            }
        }
        redisComponent.CleanDelFileList(videoId);

        esSearchComponent.SaveDoc(videoInfo);
    }

    /**
     * 根据视频id获取视频信息
     *
     * @param videoId
     */
    @Override
    public VideoInfoPost GetVideoInfoPostByVideoId(String videoId) {
        return this.videoInfoPostMapper.GetVideoPostInfoByVideoId(videoId);
    }

    //TODO 发布视频增加硬币 播放历史 21；36
}
