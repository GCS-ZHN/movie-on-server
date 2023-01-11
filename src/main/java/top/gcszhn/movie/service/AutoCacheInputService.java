package top.gcszhn.movie.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import top.gcszhn.movie.AppConfig;
import top.gcszhn.movie.utils.LogUtils;

@Service
public class AutoCacheInputService {
    /**是否在jvm退出时清理缓存 */
    @Value("${cache.reserved:true}")
    private boolean reserved;

    /**应用配置 */
    @Autowired
    private AppConfig appConfig;

    /**缓存对象 */
    private Cache cache;

    /**
     * 构造函数，获取缓存对象
     * @param cacheManager 缓存管理器
     */
    @Autowired
    public AutoCacheInputService(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("autoCacheInput");
        if (this.cache == null) {
            throw new RuntimeException("Cache 'autoCacheInput' not found or created");
        }
    }

    /**
     * 创建缓存IO流
     * @param path 资源路径
     * @return 缓存流
     */
    public AutoCacheInputStream createCacheInputStream(InputStream in, String path, long expired) throws IOException {
        final String streamKey = "stream:" + path;
        if (cache.get(streamKey, String.class) != null) {
            LogUtils.printMessage("Cache item existed", LogUtils.Level.ERROR);
            return null;
        }
        final String statusKey = "status:" + path;
        final String realCachePath = createRealCachePath(path);
        cache.put(streamKey, realCachePath);
        AutoCacheInputStream inputStream = new AutoCacheInputStream(in, realCachePath, reserved);
        inputStream.whenClosed(()->{
            // 通知其他线程缓存完成，可以读取
            synchronized(inputStream) {
                inputStream.notifyAll();
            }
            appConfig.cleanCache(cache.getName(), streamKey, expired);
            appConfig.cleanCache(cache.getName(), statusKey, expired);
            LogUtils.printMessage("Cached for " + path, LogUtils.Level.INFO);
        });
        cache.put(statusKey, inputStream);
        return inputStream;
    }

    /**
     * 获取本地缓存
     * @param path
     * @return
     */
    public InputStream getCacheInputStream(String path) {
        waitFor(path);
        String realCachePath = cache.get("stream:" + path, String.class);
        if (realCachePath == null||!new File(realCachePath).exists()) {
            return null;
        }
        try {
            return new FileInputStream(realCachePath);
        } catch (FileNotFoundException e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
        return null;
    }

    /**
     * 获取缓存大小
     * @param path 资源路径
     * @return 缓存大小，单位字节
     */
    public long getCacheSize(String path) {
        waitFor(path);
        String realCachePath = cache.get("stream:" + path, String.class);
        if (realCachePath == null) {
            return 0;
        }
        return new File(realCachePath).length();
    }

    /**
     * 创建真实缓存路径
     * @param path 资源路径
     */
    private String createRealCachePath(String path) {
        path = path.replace("/", "_").replace("\\", "_");
        return new File(appConfig.getTmpDir(), path).getAbsolutePath();
    }

    /**
     * 等待缓存完毕
     * @param path
     */
    public void waitFor(String path) {
        AutoCacheInputStream inputStream = cache.get("status:" + path, AutoCacheInputStream.class);
        if (inputStream != null) {
            synchronized(inputStream) {
                while (!inputStream.isClosed()) {
                    try {
                        LogUtils.printMessage("Waiting for cache " + path, LogUtils.Level.INFO);
                        inputStream.wait();
                    } catch (InterruptedException e) {
                        LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
                    }
                }
            }
        }
    }
}
