package com.PiliPili.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class StringToolsUtils {

    /**
     * 生成随机字符串
     */
    public static final String GetRandomString(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    /**
     * 生成随机数字
     */
    public static final String GetRandomNumber(int length) {
        return RandomStringUtils.random(length, false, true);
    }

    /**
     * 加密密码
     */
    public static final String EncodeByMD5(String originalString){
        if(originalString != null){
            return DigestUtils.md5Hex(originalString);
        }else return null;
    }

    /**
     * 路径是否合法
     * @param filePath
     */
    public static final boolean pathIsOk(String filePath){
        if (filePath == null) {
            return true;
        }
        return !filePath.contains("../") && !filePath.contains("..\\");
    }

    /**
     * 字符串首字母大写
     * @param str
     * @return
     */
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        // 将首字母转换为大写
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static boolean IsEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
