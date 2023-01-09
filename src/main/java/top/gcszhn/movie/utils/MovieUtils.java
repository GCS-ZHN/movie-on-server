package top.gcszhn.movie.utils;

import java.util.ArrayList;
import java.util.List;

public class MovieUtils {
    /**
     * 解析m3u8文件，返回url资源列表
     * @param baseUrl
     * @param m3u8Input
     * @return
     */
    public static List<String> parseHlsList(String baseUrl, String m3u8Content) {
        List<String> list = new ArrayList<>();
        String[] lines = m3u8Content.split("\n");
        for (String line : lines) {
            if (line.startsWith("#")) continue;
            if (line.startsWith("http")) {
                list.add(line);
            } else {
                list.add(baseUrl + line);
            }
        }
        return list;
    }
}
