package com.PiliPili.component;

import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.dto.VideoInfoEsDto;
import com.PiliPili.entity.enums.SearchTypeOrderEnum;
import com.PiliPili.entity.page.Page;
import com.PiliPili.entity.page.PageBean;
import com.PiliPili.entity.pojo.User;
import com.PiliPili.entity.pojo.VideoInfo;
import com.PiliPili.entity.query.UserInfoQuery;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.mapper.UserMapper;
import com.PiliPili.utils.CopyToolsUtils;
import com.PiliPili.utils.JsonUtils;
import com.PiliPili.utils.StringToolsUtils;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.json.Json;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EsSearchComponent {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private UserMapper userMapper;

    private boolean IsExistIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(appConfig.getEsIndexVideoName());
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    public void CreateIndex() {
        try {
            if (IsExistIndex()) {
                return;
            }
            //创建es index索引
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(appConfig.getEsIndexVideoName());
            createIndexRequest.settings("{\"analysis\": {\n" +
                    "    \"analyzer\": {\n" +
                    "      \"comma\": {\n" +
                    "        \"type\": \"pattern\",\n" +
                    "        \"pattern\": \",\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }}", XContentType.JSON);
            //创建视频属性映射
            createIndexRequest.mapping("{" +
                    "\n  \"properties\": {" +
                    "\n    \"videoId\": {" +
                    "\n      \"type\": \"text\"," +
                    "\n      \"index\": \"false\"" +
                    "\n    }," +
                    "\n    \"userId\": {" +
                    "\n      \"type\": \"text\"," +
                    "\n      \"index\": \"false\"" +
                    "\n    }," +
                    "\n    \"videoCover\": {" +
                    "\n      \"type\": \"text\"," +
                    "\n      \"index\": \"false\"" +
                    "\n    }," +
                    "\n    \"videoName\": {" +
                    "\n      \"type\": \"text\"," +
                    "\n      \"analyzer\": \"ik_max_word\"" +
                    "\n    }," +
                    "\n    \"tags\": {" +
                    "\n      \"type\": \"text\"," +
                    "\n      \"analyzer\": \"comma\"" +
                    "\n    }," +
                    "\n    \"playCount\": {" +
                    "\n      \"type\": \"integer\"," +
                    "\n      \"index\": \"false\"" +
                    "\n    }," +
                    "\n    \"barrageCount\": {" +
                    "\n      \"type\": \"integer\"," +
                    "\n      \"index\": \"false\"" +
                    "\n    }," +
                    "\n    \"collectCount\": {" +
                    "\n      \"type\": \"integer\"," +
                    "\n      \"index\": \"false\"" +
                    "\n    }," +
                    "\n    \"createTime\": {" +
                    "\n      \"type\": \"date\"," +
                    "\n      \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss||epoch_millis\"," +
                    "\n      \"index\": \"false\"" +
                    "\n    }" +
                    "\n  }" +
                    "\n}", XContentType.JSON);
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            boolean isAcknowledge = createIndexResponse.isAcknowledged();
            if (!isAcknowledge) {
                throw new BusinessException("初始化es失败");
            }
        } catch (Exception e) {
            log.error("初始化es失败", e);
            throw new BusinessException("初始化es失败");
        }
    }

    /**
     * 保存视频信息至es,视频审核通过后放入es
     *
     * @param videoInfo
     */
    public void SaveDoc(VideoInfo videoInfo) {
        try {
            if (DocExist(videoInfo.getVideoId())) {
                UpdateVideoInfo(videoInfo);
            } else {
                VideoInfoEsDto videoInfoEsDto = CopyToolsUtils.copy(videoInfo, VideoInfoEsDto.class);
                //视频初始数据为零
                videoInfoEsDto.setCollectCount(0);
                videoInfoEsDto.setPlayCount(0);
                videoInfoEsDto.setBarrageCount(0);
                IndexRequest request = new IndexRequest(appConfig.getEsIndexVideoName());
                //设置视频id为es文档id
                request.id(videoInfo.getVideoId()).source(JsonUtils.convertObjToJson(videoInfoEsDto), XContentType.JSON);
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            log.error("保存至es失败");
            throw new BusinessException("保存至es失败");
        }
    }

    /**
     * 视频对应的es doc是否存在
     *
     * @param id
     * @return
     * @throws IOException
     */
    public boolean DocExist(String id) throws IOException {
        GetRequest getRequest = new GetRequest(appConfig.getEsIndexVideoName(), id);
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        return getResponse.isExists();
    }

    /**
     * 更新es的视频信息
     *
     * @param videoInfo
     */
    //TODO 待测试
    private void UpdateVideoInfo(VideoInfo videoInfo) {
        try {
            videoInfo.setCreateTime(null);
            videoInfo.setLastUpdateTime(null);
            //反射实现只更新不为null的值
            Map<String, Object> map = new HashMap<>();
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                //动态获取get方法
                String methodName = "get" + StringToolsUtils.capitalizeFirstLetter(field.getName());
                Method method = videoInfo.getClass().getMethod(methodName);
                Object object = method.invoke(videoInfo);
                if (object != null && object instanceof String && StringToolsUtils.IsEmpty(object.toString()) || object != null && !(object instanceof String)) {
                    map.put(field.getName(), object);
                }
            }
            //为空则没有信息要更新
            if (map.isEmpty()) {
                return;
            }
            //更新es
            UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexVideoName(), videoInfo.getVideoId());
            updateRequest.doc(map);
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es更新视频信息失败", e);
            throw new BusinessException("es更新视频信息失败");
        }
    }

    /**
     * 更新es中视频的数量信息
     *
     * @param videoId
     * @param fieldName
     * @param count
     */
    public void UpdateDocCount(String videoId, String fieldName, Integer count) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexVideoName(), videoId);
            //直接在es更新数量
            Script script = new Script(ScriptType.INLINE, "painless", "ctx._source." + fieldName + " += params.count", Collections.singletonMap("count", count));
            updateRequest.script(script);
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es更新视频数量失败", e);
            throw new BusinessException("es更新视频数量失败");
        }
    }

    /**
     * 删除es的视频信息
     *
     * @param videoId
     */
    public void DeleteDoc(String videoId) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(appConfig.getEsIndexVideoName(), videoId);
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("es删除视频失败", e);
            throw new BusinessException("es删除视频失败");
        }
    }

    /**
     * es查询
     *
     * @param highlight
     * @param keyword
     * @param orderType
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<VideoInfo> search(Boolean highlight, String keyword, Integer orderType, Integer pageNum, Integer pageSize) {
        try {
            SearchTypeOrderEnum searchTypeOrderEnum = SearchTypeOrderEnum.GetOrderType(orderType);
            //设置ex查询条件
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "videoName", "tags"));
            //是否高亮关键词
            if (highlight) {
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.field("videoName");
                highlightBuilder.preTags("<span class = 'highlight'>");
                highlightBuilder.postTags("</span>");
                searchSourceBuilder.highlighter(highlightBuilder);
            }
            //对查询结果排序，默认按照综合得分排序
            searchSourceBuilder.sort("_score", SortOrder.ASC);
            //按照传入的参数排序 播放量，弹幕数等
            if (searchTypeOrderEnum != null) {
                searchSourceBuilder.sort(searchTypeOrderEnum.getField(), SortOrder.DESC);
            }
            //设置分页参数
            pageNum = pageNum == null ? 1 : pageNum;
            pageSize = pageSize == null ? 20 : pageSize;
            searchSourceBuilder.size(pageSize);
            searchSourceBuilder.from((pageNum - 1) * pageSize);

            SearchRequest request = new SearchRequest(appConfig.getEsIndexVideoName());
            request.source(searchSourceBuilder);

            //查询结果
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            SearchHits searchHits = response.getHits();
            Integer count = (int) searchHits.getTotalHits().value;
            List<VideoInfo> videoInfoList = new ArrayList<>();
            //保存视频用户id用于联查用户信息
            List<String> userIdList = new ArrayList<>();
            for (SearchHit searchHit : searchHits) {
                VideoInfo videoInfo = JsonUtils.convertJsonToObj(searchHit.getSourceAsString(), VideoInfo.class);
                if (searchHit.getHighlightFields().get("videoName") != null) {
                    videoInfo.setVideoName(searchHit.getHighlightFields().get("videoName").fragments()[0].string());
                }
                videoInfoList.add(videoInfo);
                userIdList.add(videoInfo.getUserId());
            }
            //查询视频发布用户的信息
            if (!userIdList.isEmpty()) {
                UserInfoQuery userInfoQuery = new UserInfoQuery();
                userInfoQuery.setUserIdList(userIdList);
                List<User> userList = this.userMapper.GetUserInfoByUserIdList(userInfoQuery);
                Map<String, User> userMap = userList.stream().collect(Collectors.toMap(item -> item.getUserId(), Function.identity(), (data1, data2) -> data2));
                //将用户名存入视频信息
                videoInfoList.forEach(item -> {
                    item.setUserName(userMap.get(item.getUserId()).getUserName());
                });
            }
            //分页

            Page<VideoInfo> videoInfoPage = new Page<>(pageNum, pageSize, videoInfoList.size(), videoInfoList);

            return videoInfoPage;
        } catch (IOException e) {
            log.error("查询失败", e);
            throw new BusinessException("查询失败");
        }
    }

}
