package top.gcszhn.movie.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import top.gcszhn.movie.AppConfig;
import top.gcszhn.movie.utils.IOUtils;
import top.gcszhn.movie.utils.HttpDataPair;
import top.gcszhn.movie.utils.LogUtils;
import top.gcszhn.movie.utils.MovieUtils;

/**
 * 百度网盘资源流后端
 */
public class BaiduPanResourceService implements ResourceService {

    @Autowired
    AsyncService asyncService;

    @Autowired
    AppConfig config;

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
                    "size", IOUtils.readableFileSize(fileObj.getLongValue("size")));
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
        final String accessToken = tmps[0];
        target = tmps[1];
        final String path = resourcePath + target;
        final String uk = baiduPanService.getUserInfo(accessToken).getString("uk");
        final String key = uk + path;
        final String base = path.substring(0, path.lastIndexOf("/") + 1);
        final String listKey = uk + base;
        InputStream inputStream = autoCacheInputService.getCacheInputStream(key);
        final Cache playlistCache = config.getCacheManager().getCache("playlist");
        
        if (playlistCache != null && playlistCache.get(listKey) != null) {
            @SuppressWarnings("unchecked")
            List<String> playlist = (List<String>) playlistCache.get(listKey).get();
            int locate = playlist.indexOf(path);
            if (locate > 0) {
                int preloadCount = 5;
                for (int i = locate + 1; i < playlist.size() && preloadCount > 0; i++, preloadCount--) {
                    String videoPath = playlist.get(i);
                    asyncService.run(()->preload(accessToken, videoPath, uk));
                }
            }
        }
        long contentLength = autoCacheInputService.getCacheSize(key);
        if (inputStream == null) {
            HttpDataPair dataPair = baiduPanService.getFileByPath(accessToken, path);
            if (dataPair != null) {
                inputStream = dataPair.getResponse().getEntity().getContent();
                inputStream = autoCacheInputService.createCacheInputStream(inputStream, key, 60 * 60 * 1000);
                // 可能被其他线程创建了缓存，所以需要再次判断
                if (inputStream != null) {
                    ((AutoCacheInputStream) inputStream).whenClosed(()->{
                        if (path.toLowerCase().endsWith(".m3u8")){
                            try (InputStream cached = autoCacheInputService.getCacheInputStream(key)) {
                                String content = new String(cached.readAllBytes(), AppConfig.DEFAULT_CHARSET);
                                List<String> playlist = MovieUtils.parseHlsList(base, content);
                                playlistCache.put(listKey, playlist);
                                config.cleanCache("playlist", listKey, 60 * 60 * 1000);
                            } catch (Exception e) {
                                LogUtils.printMessage("Cache playlist error: " + path, e, LogUtils.Level.ERROR);
                            }
                        }
                    });
                } else {
                    inputStream = autoCacheInputService.getCacheInputStream(key);
                }

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
            LogUtils.printMessage("Fetch resource failed: " + path, LogUtils.Level.ERROR);
        }
        return null;
    }

    /**
     * 预加载视频文件
     * @param accessToken
     * @param path
     * @param uk
     */
    public void preload(String accessToken, String path, String uk) {
        String key = uk + path;
        InputStream inputStream = autoCacheInputService.getCacheInputStream(key);
        if (inputStream == null) {
            LogUtils.printMessage("Try preload: "+ key);
            int max_retrial = 3;
            while (inputStream == null && max_retrial-- > 0) {
                try (HttpDataPair dataPair = baiduPanService.getFileByPath(accessToken, path)) {
                    inputStream = dataPair.getResponse().getEntity().getContent();
                    if (inputStream == null) {
                        LogUtils.printMessage("Preload failed: " + path, LogUtils.Level.ERROR);
                        Thread.sleep(1000);
                        continue;
                    }
                    inputStream = autoCacheInputService.createCacheInputStream(inputStream, key, 60 * 60 * 1000);
                    // 说明被另一个线程缓存中
                    if (inputStream == null) return;
                    // read inputstream with limited speed 500KB/s = 50K/100ms
                    byte[] buffer = new byte[1024 * 50];
                    long start = System.currentTimeMillis();
                    while (inputStream.read(buffer) != -1) {
                        long interval = System.currentTimeMillis() - start;
                        if (interval < 100) {
                            Thread.sleep(100 - interval);
                        }
                        start = System.currentTimeMillis();
                    }
                    inputStream.close();
                    LogUtils.printMessage("Preload " + path, LogUtils.Level.INFO);
                } catch (Exception e) {
                    LogUtils.printMessage("Preload error: " + path, e, LogUtils.Level.ERROR);
                }
            }
        } else {
            LogUtils.printMessage("Cache existed for " + path, LogUtils.Level.INFO);
        }
    }
}
