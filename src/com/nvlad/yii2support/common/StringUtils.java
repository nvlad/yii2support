package com.nvlad.yii2support.common;

/**
 * Created by oleg on 14.04.2017.
 */
public class StringUtils {
    public static String CamelToId(String name) {
        return CamelToId(name, "_");
    }

    public static String CamelToId(String name, String separator) {
        String regex =  "(?<=[a-z0-9])[A-Z]";
        String result = name.trim().replaceAll(regex, "_$0").toLowerCase();
        return result;
    }
}
