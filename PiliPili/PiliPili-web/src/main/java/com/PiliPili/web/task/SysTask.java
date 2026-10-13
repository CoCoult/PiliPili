package com.PiliPili.web.task;

import com.PiliPili.service.StatisticsInfoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SysTask {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    /**
     * 每天0时0分0秒定时更新总数据
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void StatisticData() {
        this.statisticsInfoService.StatisticData();
    }

    /**
     * 每天0时0分0秒定时更新用户评分矩阵
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void SaveUserScoreMatrix() {
        this.statisticsInfoService.SaveUserScoreMatrix();
    }
}
