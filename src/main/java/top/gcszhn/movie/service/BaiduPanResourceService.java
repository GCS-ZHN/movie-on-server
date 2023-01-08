package top.gcszhn.movie.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import top.gcszhn.movie.AppUtils;
import top.gcszhn.movie.utils.HttpDataPair;
import top.gcszhn.movie.utils.LogUtils;

/**
 * 百度网盘资源流后端
 */
public class BaiduPanResourceService implements ResourceService {

    @Autowired
    BaiduPanService baiduPanService;

    @Autowired
    AutoCacheInputService autoCacheInputService;

    @Override
    public List<Map<String, String>> getResourceList(String target, String resourcePath, List<String> resoureType)
            throws IOException {
        String[] tmps = target.split(":");
        String accessToken = tmps[0];
        target = tmps[1];
        String dir = resourcePath + target;
        JSONArray files = baiduPanService.getFileMetaInfoByDir(accessToken, dir, true);
        List<Map<String, String>> movies = new ArrayList<>();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:sss");
        files.forEach(file -> {
            JSONObject fileObj = (JSONObject) file;
            String filename = fileObj.getString("filename");
            String ext = filename.substring(filename.lastIndexOf(".") + 1);
            boolean isDir = fileObj.getIntValue("isdir") == 1;
            if (!resoureType.contains(ext) && !isDir) {
                return;
            }
            Map<String, String> movie = Map.of(
                    "name", filename,
                    "type", isDir ? "directory" : "file",
                    "create", dateFormat.format(fileObj.getLongValue("server_ctime") * 1000),
                    "size", AppUtils.readableFileSize(fileObj.getLongValue("size")));
            if (movie != null) {
                movies.add(movie);
            }
        });
        return movies;
    }
    
    @Override
    public ResponseEntity<InputStreamResource> getResourceStream(String target, String resourcePath)
            throws IOException, URISyntaxException {
        String[] tmps = target.split(":");
        String accessToken = tmps[0];
        target = tmps[1];
        String path = resourcePath + target;
        InputStream inputStream = autoCacheInputService.getCacheInputStream(path);
        long contentLength = autoCacheInputService.getCacheSize(path);
        if (inputStream == null) {
            HttpDataPair dataPair = baiduPanService.getFileByPath(accessToken, path);
            if (dataPair != null) {
                inputStream = dataPair.getResponse().getEntity().getContent();
                inputStream = autoCacheInputService.createCacheInputStream(inputStream, path, 60 * 60 * 1000);
                contentLength = dataPair.getResponse().getEntity().getContentLength();
            }
        } else {
            LogUtils.printMessage("Cache hinted for " + path, LogUtils.Level.INFO);
        }
        if (inputStream != null) {
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/octet-stream")
                    .contentLength(contentLength)
                    .body(resource);

        } else {
            LogUtils.printMessage("获取文件失败：" + path, LogUtils.Level.ERROR);
        }
        return null;
    }
}
