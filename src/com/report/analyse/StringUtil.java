package com.report.analyse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**i字符串操作类*/
public class StringUtil {

    /**截取src的第一个在start和end之间的字符串
     * */
    public static String getBetween(String src, String start, String end) {
        int i = src.indexOf(start);
        if (i == -1) return null;
        int j = src.indexOf(end, i + start.length());
        if (j == -1) return null;
        return src.substring(i + start.length(), j);
    }
    /**截取src的所有在start和end之间的字符串，以列表返回
     * */
    public static List<String> getSpiltList(String src, String start, String end) {
        ArrayList<String> list = new ArrayList<>();
        while (src != null && !src.equals("")) {
            String cut = getBetween(src, start, end);
            if (cut == null) return list;
            list.add(cut);
            src = src.substring(src.indexOf(end,src.indexOf(start)) + end.length());
        }
        return list;
    }
    /**截取src的第一个在spilt之后的字符串
     * */
    public static String getAfter(String src, String spilt) {
        if(src==null)return null;
        int i = src.indexOf(spilt);
        if (i == -1) return null;
        return src.substring(i + spilt.length());
    }
    /**截取src的第一个在spilt之前的字符串
     * */
    public static String getBefore(String src, String spilt) {
        int i = src.indexOf(spilt);
        if (i == -1) return null;
        return src.substring(0, i);
    }

}
