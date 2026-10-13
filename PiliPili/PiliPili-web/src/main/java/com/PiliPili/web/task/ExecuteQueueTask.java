package com.PiliPili.web.task;

import com.PiliPili.component.EsSearchComponent;
import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.VideoPlayInfoDto;
import com.PiliPili.entity.enums.SearchTypeOrderEnum;
import com.PiliPili.entity.pojo.VideoInfoFilePost;
import com.PiliPili.entity.pojo.VideoPlayHistory;
import com.PiliPili.service.VideoInfoPostService;
import com.PiliPili.service.VideoInfoService;
import com.PiliPili.service.VideoPlayHistoryService;
import com.PiliPili.utils.StringToolsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class ExecuteQueueTask {
    private ExecutorService executorService = Executors.newFixedThreadPool(Constants.ONE * 2);

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    @Resource
    private VideoInfoService videoInfoService;

    /**
     * 消息队列处理视频转码
     */
    @PostConstruct
    public void ConsumeTransferFileQueue() {
        executorService.execute(() -> {
            while (true) {
                try {
                    VideoInfoFilePost videoInfoFilePost = redisComponent.GetFileFromPostQueue();
                    if(videoInfoFilePost == null) {
                        Thread.sleep(1500);
                        continue;
                    }
                    videoInfoPostService.TransferVideoFile(videoInfoFilePost);
                } catch (Exception e) {
                    log.error("获取转码文件队列信息失败");
                }
            }
        });
    }

    /**
     * 消息队列处理视频历史记录
     */
    @PostConstruct
    public void ConsumeVideoPlayQueue() {
        executorService.execute(() -> {
            while (true) {
                try {
                    VideoPlayInfoDto videoPlayInfoDto = redisComponent.GetVideoPlayFromQueue();
                    if(videoPlayInfoDto == null) {
                        Thread.sleep(1500);
                        continue;
                    }
                    this.videoInfoService.addVideoPlayCount(videoPlayInfoDto.getVideoId());
                    if(!StringToolsUtils.IsEmpty(videoPlayInfoDto.getUserId())) {
                        this.videoPlayHistoryService.SaveVideoHistory(videoPlayInfoDto.getUserId(), videoPlayInfoDto.getVideoId(), videoPlayInfoDto.getFileIndex());
                    }
                    //按天记录视频播放量
                    redisComponent.recordVideoPlayCount(videoPlayInfoDto.getVideoId());
                    //更新es播放量
                    esSearchComponent.UpdateDocCount(videoPlayInfoDto.getVideoId(), SearchTypeOrderEnum.VIDEO_PLAY.getField(), 1);

                } catch (Exception e) {
                    log.error("获取历史记录队列信息失败");
                }
            }
        });
    }


    //TODO 视频模块后补 视频历史记录功能

}
