package com.PiliPili.utils;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static String convertObjToJson(Object object) {
        return JSON.toJSONString(object);
    }

    public static <T> T convertJsonToObj(String json, Class<T> cla) {
        return JSON.parseObject(json, cla);
    }

    public static <T> List<T> convertJsonArrayToList(String json, Class<T> cla) {
        return JSON.parseArray(json, cla);
    }
}
