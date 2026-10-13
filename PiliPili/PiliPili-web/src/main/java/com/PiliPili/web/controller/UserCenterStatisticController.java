package com.PiliPili.web.controller;

import com.PiliPili.annotation.GlobalInterceptor;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.pojo.StatisticsInfo;
import com.PiliPili.entity.query.StatisticInfoQuery;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.service.StatisticsInfoService;
import com.PiliPili.utils.DateUtils;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.scanner.Constant;

import javax.annotation.Resource;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/userCenter")
public class UserCenterStatisticController extends BaseController {
    @Resource
    private StatisticsInfoService statisticsInfoService;

    /**
     * 获取前一天和总数据信息
     *
     * @return
     */
    @RequestMapping("/getAllStatisticInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result GetAllStatisticInfo() {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();

        String preDate = DateUtils.GetBeforeDate(1);
        StatisticInfoQuery statisticInfoQuery = new StatisticInfoQuery();
        statisticInfoQuery.setStatisticsDate(preDate);
        statisticInfoQuery.setUserId(tokenUserInfoDto.getUserId());

        List<StatisticsInfo> statisticsInfoList = this.statisticsInfoService.GetStatisticInfoByParam(statisticInfoQuery);
        //前一天的数据
        Map<Integer, Integer> preDateStaMap = statisticsInfoList.stream().collect(Collectors.toMap(StatisticsInfo::getDataType, StatisticsInfo::getStatisticsCount, (item1, item2) -> item2));
        //全部数据
        Map<String, Integer> totalCountInfo = this.statisticsInfoService.GetAllStatistic(tokenUserInfoDto.getUserId());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("preDayData", preDateStaMap);
        resultMap.put("totalCountInfo", totalCountInfo);
        return Result.success(resultMap);
    }

    /**
     * 获取一周内的数据
     * @param dateType
     * @return
     */
    @RequestMapping("/getWeeksStatisticInfo")
    @GlobalInterceptor(checkLogin = true)
    public Result GetWeeksStatisticInfo(Integer dateType) {
        TokenUserInfoDto tokenUserInfoDto = GetTokenUserInfo();

        List<String> dateList = DateUtils.GetBeforeDates(Constants.ONE * 7);
        StatisticInfoQuery statisticInfoQuery = new StatisticInfoQuery();
        statisticInfoQuery.setDataType(dateType);
        statisticInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        statisticInfoQuery.setStatisticsDateStart(dateList.get(0));
        statisticInfoQuery.setStatisticsDateEnd(dateList.get(dateList.size() - 1));
        statisticInfoQuery.setOrderBy("statistics_date asc");
        List<StatisticsInfo> statisticsInfoList = this.statisticsInfoService.GetStatisticInfoByParam(statisticInfoQuery);
        Map<String, StatisticsInfo> dataMap = statisticsInfoList.stream().collect(Collectors.toMap(item -> item.getStatisticsDate(), Function.identity(), (data1, data2) -> data2));

        List<StatisticsInfo> resultDate = new ArrayList<>();
        for (String date : dateList) {
            StatisticsInfo dateItem = dataMap.get(date);
            if (dateItem == null) {
                dateItem = new StatisticsInfo();
                dateItem.setStatisticsDate(date);
                dateItem.setStatisticsCount(0);
            }
            resultDate.add(dateItem);
        }
        return Result.success(resultDate);
    }


}
