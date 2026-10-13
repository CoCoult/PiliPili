package com.PiliPili.entity.query;

import lombok.Getter;

@Getter
public class StatisticInfoQuery {
    private String statisticsDate;

    private String userId;

    private Integer dataType;

    private Integer statisticsCount;

    private String statisticsDateStart;

    private String statisticsDateEnd;

    private String orderBy;

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void setStatisticsDateStart(String statisticsDateStart) {
        this.statisticsDateStart = statisticsDateStart;
    }

    public void setStatisticsDateEnd(String statisticsDateEnd) {
        this.statisticsDateEnd = statisticsDateEnd;
    }

    public void setStatisticsDate(String statisticsDate) {
        this.statisticsDate = statisticsDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public void setStatisticsCount(Integer statisticsCount) {
        this.statisticsCount = statisticsCount;
    }
}
